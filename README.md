[FR](#api-gateway-fr) | [EN](#api-gateway-en)

# API Gateway (FR)

## Architecture
Ce microservice fait partie d'une application de gestion de données médicales et démographiques permettant d'obtenir des rapports de risques en fonction des profils des patients et de leurs constatations médicales.

Il s'intègre à l'application avec d'autres microservices :
 - [Microservice Patient](https://github.com/divineion/abernathyclinic-patient) pour la gestion des données démographiques des patients. 
 - [Microservice Notes](https://github.com/divineion/abernathyclinic-notes) pour la gestion des données médicales.  
 - [Microservice Report](https://github.com/divineion/abernathyclinic-report) pour l'évaluation du niveau de risque de diabète en croisant les données démographiques et les notes médicales.   
 - [Infrastructure](https://github.com/divineion/abernathyclinic-infra) pour l'orchestration Docker.   
 - [Interface utilisateur](https://github.com/divineion/abernathyclinic-client) pour l'interface web de gestion des fiches patients et la consultation des rapports de risque.   
 
 ![Schéma d'architecture](docs/app-architecture.png) 
 
 
## 1. Rôle
L'API Gateway constitue le point d'entrée du système : 
 - routage et réécriture d'URL vers les microservices cibles (Patient, Notes et Report),
 - authentification HTTP Basic et gestion des règles CORS,
 - propagation du contexte de sécurité (injection des identifiants et rôles utilisateurs via les en-têtes `X-Auth-User-Roles` et `X-Auth-User-Id`),
 - gestion des secrets avec HashiCorp Vault (écriture et lecture des identifiants utilisateurs dans le moteur Key-Value). 

## 2. Choix techniques
 - Langage : **Java 24**
 - Framework : **SpringBoot** (Spring Cloud Gateway Server Webflux, Spring Security Reactive)
 - Gestion des secrets : **HashiCorp Vault**, Spring Vault Reactive
 - Tests d'intégration : **Wiremock**, **Testcontainers** pour le module Vault
 - Conteneurisation : **Docker**

## 3. Configuration
La configuration est définie dans `application.yaml` et ses déclinaisons, et complétée par un fichier `.env`.

Copiez `.env.example` vers `.env` pour renseigner vos variables locales : 
`ORGANIZER1_PASSWORD` | Mot de passe de l'utilisateur de démo organizer1 | **à renseigner**   
`ORGANIZER2_PASSWORD` | Mot de passe de l'utilisateur de démo organizer2 | **à renseigner**   
`ORGANIZER3_PASSWORD` | Mot de passe de l'utilisateur de démo organizer3 | **à renseigner**   

`DOCTOR1_PASSWORD` | Mot de passe de l'utilisateur de démo doctor1 | **à renseigner**   
`DOCTOR2_PASSWORD` | Mot de passe de l'utilisateur de démo doctor2 | **à renseigner**    
`DOCTOR3_PASSWORD` | Mot de passe de l'utilisateur de démo doctor3 | **à renseigner**   

Pour la configuration de Vault, voir le [guide de configuration locale](docs/vault-setup.md)

`DEV_VAULT_ENDPOINT_HOST` | Hôte du serveur Vault (profil dev) | `127.0.0.1`   
`DEV_VAULT_ENDPOINT_SCHEME` | Protocole HTTP Vault (profil dev) | `http`   
`DEV_VAULT_ENDPOINT_PORT` | Port d'écoute Vault (profil dev) | `8200`   

`DEV_SPRING_USER_CREATOR_VAULT_TOKEN` | Token Vault avec droits d'écriture (création users) | **à renseigner**   
`DEV_SPRING_USER_READER_VAULT_TOKEN` | Token Vault avec droits de lecture seule | **à renseigner**   

`DEV_VAULT_USERS_KV_PATH` | Chemin du moteur KV Vault pour les utilisateurs | `secret/abernathyclinic-gateway/dev/users/`   

`DOCKER_VAULT_ENDPOINT_HOST` | Hôte du serveur Vault (environnement Docker) | `127.0.0.1`   
`DOCKER_VAULT_ENDPOINT_SCHEME` | Protocole HTTP Vault (environnement Docker) | `http`   
`DOCKER_VAULT_ENDPOINT_PORT` | Port d'écoute Vault (environnement Docker) | `8300`   

`DOCKER_SPRING_USER_CREATOR_VAULT_TOKEN` | Token d'écriture Vault (Docker) | **à renseigner**   
`DOCKER_SPRING_USER_READER_VAULT_TOKEN` | Token de lecture Vault (Docker) | **à renseigner**   

`DOCKER_VAULT_USERS_KV_PATH` | Chemin KV Vault pour Docker | `secret/abernathyclinic-gateway/docker/users/`

## 4. Principaux endpoints
L'API Gateway intercepte les requêtes publiques et les réécrit vers les endpoints des microservices. 

GET `/user` : retourne l'état d'authentification, le nom d'utilisateur et les rôles de la session courante.

`/patient/**` : redirige vers le Microservice Patient (`http://localhost:8081/api/...`).
`/patients` : redirige vers la liste des patients (`http://localhost:8081/api/patients`).
`/note/**`, `/notes/**` : redirige vers le Microservice Notes (`http://localhost:8083/api/...`).
`/report/**` : redirige vers le Microservice Report (`http://localhost:8084/api/...`).

## 5. Démarrage rapide
### Prérequis
 - Java 24
 - Maven 3.x
 - Instance HashiCorp Vault démarrée et déverrouillée
 - Fichier `.env` complété

### Lancer l'API Gateway
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

[EN](#api-gateway-en) | [FR](#api-gateway-fr)
# API Gateway (EN)
