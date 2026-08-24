[FR](#guide-de-configuration-locale-pour-hashicorp-vault) | [EN](#local-setup-guide-for-hashicorp-vault)

# Guide de configuration locale pour HashiCorp Vault

Vault sert de stockage sécurisé pour les identifiants et les rôles des utilisateurs de l'application (comptes `DOCTOR` et `ORGANIZER`), utilisés par l'API Gateway pour l'authentification. 

Cette documentation indique les étapes d'installation, de configuration et d'initialisation pour générer des tokens d'accès au stockage Vault.    

## 1. Prérequis
 - Linux ou WSL2 (Ubuntu recommandé)
 - Privilèges `sudo`
 - `gnome-terminal` et `jq` (requis pour le script de démarrage automatique)
 
 Installer Vault en suivant la [documentation officielle](https://developer.hashicorp.com/vault/install#linux).

## 2. Configuration des répertoires et du serveur
### 2.1 Répertoires de stockage
``` 
sudo mkdir -p /opt/vault/data-dev /opt/vault/data-docker /etc/vault.d

sudo chown -R $(whoami):$(whoami) /opt/vault /etc/vault.d
```

### 2.2 Fichiers de configuration
#### Environnement dev
Créer le fichier de configuration : 

```
touch /etc/vault.d/vault-dev.hcl
```

Renseigner le port d'écoute et l'emplacement du stockage dans `vault-dev.hcl` :

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

#### Environnement Docker
Créer le fichier de configuration :

```
touch /etc/vault.d/vault-docker.hcl
```

Renseigner le port d'écoute et l'emplacement du stockage dans `vault-docker.hcl` :

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

## 3. Démarrage et initialisation du serveur
Note : `dev` fait référence au profil Spring `dev`, et non au serveur dev in-memory de Vault. 

### 3.1 Démarrage du serveur
#### Environnement dev
Dans le terminal 1 : 

```
vault server -config=/etc/vault.d/vault-dev.hcl
```

#### Environnement Docker
Dans le terminal 1 : 

```
vault server -config=/etc/vault.d/vault-docker.hcl
```

### 3.2 Initialiser Vault
#### Environnement dev
Dans le terminal 2 : 

```
export VAULT_ADDR='http://localhost:8200'
export VAULT_API_ADDR='http://localhost:8200'
vault operator init
```

#### Environnement Docker
```
export VAULT_ADDR='http://localhost:8300'
export VAULT_API_ADDR='http://localhost:8300'
vault operator init
```

**IMPORTANT** : Vault génère 5 clés de déverrouillage (unseal keys) et un root token **à conserver en lieu sûr**. 

### 3.3 Déverrouillage du Vault et connexion root
Déverrouiller le coffre-fort en fournissant 3 unseal keys distinctes :    

```
vault operator unseal <unseal_key_1>
vault operator unseal <unseal_key_2>
vault operator unseal <unseal_key_3>

vault login <root_token>
```

## 4. Activation du moteur de secrets key-value (KV1)
Activer le moteur Key-Value engine (version 1) sous le chemin `/secret` :

```
vault secrets enable -version=1 -path=secret kv
```

## 5. Règles d'accès et tokens
### 5.1 Création des fichiers de règles

```
mkdir -p ~/vault-policies/abernathyclinic/
```

```
touch ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl
touch ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl

touch ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl
touch ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl
```

#### Environnement dev 
Créer le fichier de règles d'accès en écriture : 

`nano ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl`

Définir les règles dans `dev-spring-user-creator.hcl`:

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

Créer le fichier de règles d'accès en lecture : 
`nano ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl`

Définir les règles d'accès dans `dev-spring-user-reader.hcl` : 

```
path "secret/abernathyclinic-gateway/dev/users/*" {
  capabilities = ["read"]
}
```

#### Environnement Docker
Créer le fichier de règles d'accès en écriture : 
`nano ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl`

Définir les règles d'accès dans `docker-spring-user-creator.hcl`:

```
path "auth/token/create" {
  capabilities=["create", "update"]
}

path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list", "recover"]
}
```

Créer le fichier de règles d'accès en lecture : 
`nano ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl`

Définir les règles d'accès dans `docker-spring-user-reader.hcl`

```
path "secret/abernathyclinic-gateway/docker/users/*" {
  capabilities = ["read"]
}
```

### 5.2 Application des règles d'accès et génération des tokens
#### Environnement dev
Enregistrer les règles d'accès : 
`vault policy write dev-spring-user-creator ~/vault-policies/abernathyclinic/dev-spring-user-creator.hcl`
`vault policy write dev-spring-user-reader ~/vault-policies/abernathyclinic/dev-spring-user-reader.hcl`

Créer un jeton avec les droits de lecture/écriture :
`vault token create -policy=dev-spring-user-creator`

Créer un jeton avec les droits de lecture seule :
`vault token create -policy=dev-spring-user-reader`

#### Environnement Docker
Enregistrer les règles d'accès : 
`vault policy write docker-spring-user-creator ~/vault-policies/abernathyclinic/docker-spring-user-creator.hcl`
`vault policy write docker-spring-user-reader ~/vault-policies/abernathyclinic/docker-spring-user-reader.hcl`

Créer un jeton avec les droits de lecture/écriture :
`vault token create -policy=docker-spring-user-creator`

Créer un jeton avec les droits de lecture seule :
`vault token create -policy=docker-spring-user-reader`

### 5.3 Variables d'environnement
Variables à renseigner dans le `.env` : 
  
DEV_SPRING_USER_CREATOR_VAULT_TOKEN | token d'accès pour la création d'utilisateurs | votre creator_token    
DEV_SPRING_USER_READER_VAULT_TOKEN | token d'accès pour la lecture des données utilisateurs | votre reader_token    
DEV_VAULT_USERS_KV_PATH | chemin vers le stockage des données utilisateurs | `secret/abernathyclinic-gateway/dev/users/`    

DOCKER_SPRING_USER_CREATOR_VAULT_TOKEN | token d'accès pour la création d'utilisateurs (Docker) | votre creator_token    
DOCKER_SPRING_USER_READER_VAULT_TOKEN | token d'accès pour la lecture de données utilisateurs | votre reader_token      
DOCKER_VAULT_USERS_KV_PATH | chemin vers le stockage des données utilisateurs | `secret/abernathyclinic-gateway/docker/users/`    


## 6. Script de démarrage automatique
Pour démarrer et déverrouiller rapidement le serveurVault en développement, vous pouvez utiliser le script suivant.   
Enregistrez-le sous `start-vault-dev.sh`, et rendez-le exécutable (`chmod +x start-vault-dev.sh`).

### 6.1 Prérequis
 - gnome-terminal installé (pour l'ouverture de nouveaux onglets dans le terminal).
 - jq installé (pour l'analyse de la sortie JSON),
 - le dossier de stockage `/opt/vault/data-dev` existant,
 - le fichier `/etc/vault.d/vault-dev.hcl` [configuré](#2-2-fichiers-de-configuration),
 - 3 clés de déverrouillage enregistrées sous `.vault-tokens/abernathyclinic/dev/unseal-key1`, `.vault-tokens/abernathyclinic/dev/unseal-key2` et `.vault-tokens/abernathyclinic/dev/unseal-key3`.
 
### 6.2 Code du script (`start-vault-dev.sh`)

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



[EN](#local-setup-guide-for-hashicorp-vault) | [FR](#guide-de-configuration-locale-pour-hashicorp-vault)

# Local Setup Guide for HashiCorp Vault
Vault is used as a secure storage engine for application credentials and user roles (`DOCTOR` and `ORGANIZER`), consumed by the API Gateway for authentication. 

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
path "secret/abernathyclinic-gateway/dev/users/*" {
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