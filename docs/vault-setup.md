
# Local Setup Guide for HashiCorp Vault
This guide explains how to install, configure, and initialize HashiCorp Vault to generate access tokens in both development and Docker environments.

## 1. Prerequisites
 - Linux or WSL2 (Ubuntu recommended)
 - `sudo` privileges
 - `gnome-terminal` and `jq` (required for automated startup script)

Install Vault following the [official documentation](https://developer.hashicorp.com/vault/install#linux).


## 2. Directories and server configuration
### 2.1 Storage directories

``` 
sudo mkdir -p /opt/vault/data-dev /opt/vault/data-docker /etc/vault.d

sudo chown -R $(whoami):$(whoami) /opt/vault /etc/vault.d
```

### 2.2 Server configuration files
#### Dev environment
Create ` /etc/vault.d/vault-dev.hcl` (local dev on port `8200`).

Add configuration to `vault-dev.hcl`:

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

#### Docker environment

```
touch /etc/vault.d/vault-docker.hcl
```

Add configuration to `vault-docker.hcl` :

```
ui = true


storage "file" {
  path = "/opt/vault/data-docker"
}

listener "tcp" {
  address = "127.0.0.1:8300"
  tls_disable = 1
}
```

## 3. Server startup and initialization
Note: dev refers to **Spring dev profile**, not to Vault dev server. 

### 3.1 Start the server
#### Dev instance
In terminal 1: 

```
vault server -config=/etc/vault.d/vault-dev.hcl
```

#### Docker instance

In terminal 1: 

```
vault server -config=/etc/vault.d/vault-docker.hcl
```

### 3.2 Initialize Vault
#### Dev instance
In terminal 2: 

```
export VAULT_ADDR='http://localhost:8200'
export VAULT_API_ADDR='http://localhost:8200'
vault operator init
```


#### Docker instance
In terminal 2:
 
```
export VAULT_ADDR='http://localhost:8300'
export VAULT_API_ADDR='http://localhost:8300'
vault operator init
```

**IMPORTANT** : Vault outputs **5 unseal keys** and **1 initial root token** : save them **securely**.

### 3.3 Unseal and root login
Unseal the storage by providing 3 distinct unseal keys: 

```
vault operator unseal <unseal_key_1>
vault operator unseal <unseal_key_2>
vault operator unseal <unseal_key_3>

vault login <root_token>
```

## 4. Enable KV Secret Engine (v1)
Enable Key-Value engine (version 1) under the `/secret` path:

```
vault secrets enable -version=1 -path=secret kv
```

## 5. Policies and tokens
### 5.1 Create policy files

```
mkdir -p ~/vault-policies/abernathyclinic/
```

```
touch ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl
touch ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl

touch ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl
touch ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl
```

#### Dev environment

`nano ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl`

Add configuration to `dev-spring-user-creator.hcl`:

```
# allow policy to generate child tokens
path "auth/token/create" {
  capabilities=["create", "update"]
}

# allow policy to create, read, update, delete and list users secrets under dev environment
path "secret/abernathyclinic-gateway/dev/users/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list", "recover"]
}
```

`nano ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl`

Fill `dev-spring-user-reader.hcl` : 

```
path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["read"]
}
```

#### Docker environment
`nano ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl`

Fill `docker-spring-user-creator.hcl`:

```
path "auth/token/create" {
  capabilities=["create", "update"]
}

path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list", "recover"]
}
```


`nano ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl`

Add configuration to `docker-spring-user-reader.hcl`

```
path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["read"]
}
```

### 5.2 Apply policies and generate tokens
#### Dev environment
Create policies using the previously created policy files
`vault policy write dev-spring-user-creator ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl`
`vault policy write dev-spring-user-reader ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl`

Create a token with read/write access policy
`vault token create -policy=dev-spring-user-creator`

Create a token with read access policy
`vault token create -policy=dev-spring-user-reader`

#### Docker environment

Create policies using the previously created policy files
`vault policy write docker-spring-user-creator ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl`
`vault policy write docker-spring-user-reader ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl`

Create a token with read/write access policy
`vault token create -policy=docker-spring-user-creator`

Create a token with read access policy
`vault token create -policy=docker-spring-user-reader`

### 5.3 Environment variables
Complete `.env` : 
  
DEV_SPRING_USER_CREATOR_VAULT_TOKEN | access token for user creation | your creator token    
DEV_SPRING_USER_READER_VAULT_TOKEN | access token for user reading | your reader token    
DEV_VAULT_USERS_KV_PATH | path to users storage | `secret/abernathyclinic-gateway/dev/users/`    

DOCKER_SPRING_USER_CREATOR_VAULT_TOKEN | access token for user creation in docker env | your creator token    
DOCKER_SPRING_USER_READER_VAULT_TOKEN | access token for user reading in docker env | your reader token    
DOCKER_VAULT_USERS_KV_PATH | path to users storage | `secret/abernathyclinic-gateway/docker/users/`    

## 6. Automated Vault Startup script (`start-vault-dev.sh`)
To quickly start and unseal your Vault dev server, you can use the following script.

Save it as `start-vault-dev.sh` and make it executable (`chmod +x start-vault-dev.sh`).

### 6.1 Prerequisites
 - gnome-terminal installed (for opening new terminal tabs),  
 - jq installed (for parsing JSON output),
 - existing `/opt/vault/data-dev` data storage
 - configured `/etc/vault.d/vault-dev.hcl`
 - 3 unseal keys saved in `.vault-tokens/abernathyclinic/dev/unseal-key1`, `.vault-tokens/abernathyclinic/dev/unseal-key2` and `.vault-tokens/abernathyclinic/dev/unseal-key3`.

```
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

if [ ! -f ".vault-tokens/abernathyclinic/dev/unseal-key1" ]; then
  echo "Error: unseal-key1 not found at .vault-tokens/abernathyclinic/dev/unseal-key1"
  exit 1
fi
echo '-------- UNSEAL 1/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key1)

if [ ! -f ".vault-tokens/abernathyclinic/dev/unseal-key2" ]; then
  echo "Error: unseal-key2 not found at .vault-tokens/abernathyclinic/dev/unseal-key2"
  exit 1
fi
echo '-------- UNSEAL 2/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key2)

if [ ! -f ".vault-tokens/abernathyclinic/dev/unseal-key3" ]; then
  echo "Error: unseal-key3 not found at .vault-tokens/abernathyclinic/dev/unseal-key3"
  exit 1
fi
echo '-------- UNSEAL 3/3 --------'
vault operator unseal $(cat .vault-tokens/abernathyclinic/dev/unseal-key3)

STATUS=\$(vault status -format=json | jq -r .sealed)
echo 'Vault sealed status = '\$STATUS
exec bash
"
```