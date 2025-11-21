# https://www.baeldung.com/ops/docker-entrypoint-environment-variables
# this specifies the base image that the build will extend
FROM eclipse-temurin:24-jre

LABEL description="API Gateway providing routing and authentication for AbernathyClinic Application"
# create if not exists + définit le dossier courant dans l'image (les commandes seront exécutées depuis /app)
WORKDIR /app
#EXPOSE instruction doesn't actually publish the port. It functions as a type of documentation
EXPOSE 8080

#COPY <host-path> <image-path> - this instruction tells the builder to copy files from the host 
# and put them into the container image --> copier le jar (fichier sur le host) dans l'image (le WORKDIR, /app)
COPY ./target/abernathyclinic-gateway-0.0.1-SNAPSHOT.jar app.jar

# point d'entrée du conteneur = spécifie la commande qui sera exécutée au lancement du conteneur
# Exec Form : on spécifie la commande et les arguments dans un array
# shell form --> pratique si on a besoin du shell mais nécessite un temps de lancement qui peut faire ouch
ENTRYPOINT ["java", "-jar", "app.jar" ]