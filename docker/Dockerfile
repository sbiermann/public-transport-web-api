FROM ibm-semeru-runtimes:open-21.0.5_11-jre-focal

RUN useradd -u 2000 nonroot -g 65534

RUN mkdir /app
ADD publictransportapi.jar /app/publictransportapi.jar

WORKDIR  /app

# Notwendig, damit runasNonRoot die Verwendeten User und Grupe richtig prüfen kann
USER 2000:65534

ENTRYPOINT exec java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app/publictransportapi.jar
