# Local Setup Guide for HashiCorp Vault
This guide explains how to install, configure, and initialize HashiCorp Vault locally so it can be used by the Abernathyclinic microservices running in Docker.

## Requirements
Linux (or enabled WSL on Windows)
sudo access

## Install Vault
Follow the official installation guide : 
https://developer.hashicorp.com/vault/install#linux


## Configure HashiCorp Vault
### Create directories for Vault data and config:

```bash 
sudo mkdir -p /opt/vault/data-dev
sudo mkdir -p /opt/vault/data-docker
sudo mkdir -p /etc/vault.d

sudo chown -R $(whoami):$(whoami) /opt/vault
sudo chown -R $(whoami):$(whoami) /etc/vault.d
```

### Create configuration files
```
touch /etc/vault.d/vault-dev.hcl
touch /etc/vault.d/vault-docker.hcl
```
Add the following configurations :   
Local dev : `nano /etc/vault.d/vault-dev.hcl`:
vault-dev.hcl :

```
ui = true

storage "file" {
  path = "/opt/vault/data-dev"
}

listener "tcp" {
  address = "127.0.0.1:8200"
  tls_disable = 1
}
```

Docker environment : `nano /etc/vault.d/vault-docker.hcl`   
vault-docker.hcl:
```bash 
ui = true


storage "file" {
  path = "/opt/vault/data-docker"
}

listener "tcp" {
  address = "127.0.0.1:8300"
  tls_disable = 1
}
```

## Start Vault servers
### Dev Vault
Dev refers to Spring dev profile, not to Vault dev server. 

Terminal 1 : `vault server -config=/etc/vault.d/vault-dev.hcl`

Terminal 2 : 
```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_API_ADDR='http://localhost:8200'
```


### Docker Vault
Terminal 3 : `vault server -config=/etc/vault.d/vault-docker.hcl`

Terminal 4 : 
```bash
export VAULT_ADDR='http://localhost:8300'
export VAULT_API_ADDR='http://localhost:8300'
```

## Initialize Vault (for each server in a new terminal)
`vault operator init`
Save the 5 unseal keys and root token (you will need them to unseal Vault after restarts). 
See Automated Vault Startup script.

`vault login <root_token>`

## Enable KV secrets (v1)
`vault secrets enable -version1 -path=secret kv`

## Create policies and tokens
### Create policy files

```bash
mkdir -p ~/vault-policies/abernathyclinic/

touch vault-policies/abernathyclinic/dev-spring-user-creator.hcl
touch vault-policies/abernathyclinic/dev-spring-user-reader.hcl

touch vault-policies/abernathyclinic/docker-spring-user-creator.hcl
touch vault-policies/abernathyclinic/docker-spring-user-reader.hcl
``̀

### Add policies to Vault
Dev environment
`nano vault-policies/abernathyclinic/dev-spring-user-creator.hcl`

```bash
# allow policy to generate child tokens
path "auth/token/create" {
  capabilities=["create", "update"]
}

# allow policy to create, read, update, delete and list users secrets under dev environment
path "secret/abernathyclinic-gateway/dev/users/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list", "recover"]
}
```

`vault policy write dev-spring-user-reader vault-policies/abernathyclinic/dev-spring-user-reader.hcl`

```bash
path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["read"]
}
```


Docker environment
`nano vault-policies/abernathyclinic/docker-spring-user-creator.hcl`


```bash
path "auth/token/create" {
  capabilities=["create", "update"]
}

path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list", "recover"]
}
```


`nano vault-policies/abernathyclinic/docker-spring-user-reader.hcl`


```bash
path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["read"]
}
```

`vault policy write docker-spring-user-reader vault-policies/abernathyclinic/docker-spring-user-reader.hcl`

### Apply policies and create tokens
Dev environment

`vault policy write dev-spring-user-creator ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl`

Create a token with read/write access policy and login
`vault token create -policy=dev-spring-user-creator`
`vault login <dev-spring-user-creator-token>`

`vault token create -policy=dev-spring-user-reader`

Docker environment

Create a token with read/write access policy and login
`vault token create -policy=docker-spring-user-creator`
`vault login <docker-spring-user-creator-token>`

`vault policy write docker-spring-user-creator ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl`


`vault token create -policy=docker-spring-user-reader`


Complete `.env` :   
DEV_SPRING_USER_CREATOR_VAULT_TOKEN=<creator_token>
DEV_SPRING_USER_READER_VAULT_TOKEN=<reader_token>
DOCKER_SPRING_USER_CREATOR_VAULT_TOKEN=<creator_token>
DOCKER_SPRING_USER_READER_VAULT_TOKEN=<reader_token>

DEV_VAULT_USERS_KV_PATH=secret/abernathyclinic-gateway/dev/users/
DOCKER_VAULT_USERS_KV_PATH=secret/abernathyclinic-gateway/docker/users/

# Automated Vault Startup script
To quickly start and unseal your Vault dev server, you can use the following script.
Save it as start-vault-dev.sh and make it executable (`chmod +x start-vault-dev.sh`).

## Requirements
 - gnome-terminal installed (for opening new terminal tabs).
 - jq installed (for parsing JSON output).

Unseal keys saved in .vault-tokens/abernathyclinic/dev/unseal-key1, unseal-key2, unseal-key3.


```bash
if ss -ltn | grep -q ':8200'; then
  echo "Port 8200 already in use. Trying to stop existing process."
  PID=$(sudo lsof -t -i:8200)
  if [ -n "$PID" ]; then
    echo "Kill process $PID"
    sudo kill -9 $PID
  else
    echo "Port in use but no PID found."
    sleep 3
  fi
fi

gnome-terminal -- bash -c "vault server -config=/etc/vault.d/vault-dev.hcl; exec bash"

sleep 3

gnome-terminal --tab -- bash -c "
echo 'export VAULT_ADDR=http://localhost:8200'
export VAULT_ADDR=http://localhost:8200
echo '-------- UNSEAL 1/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key1)
echo '-------- UNSEAL 2/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key2)
echo '-------- UNSEAL 3/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key3)
STATUS=\$(vault status -format=json | jq -r .sealed)
echo 'Vault sealed status = '\$STATUS
exec bash
"
```