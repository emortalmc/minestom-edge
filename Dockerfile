FROM --platform=$TARGETPLATFORM azul/zulu-openjdk:21-jre

RUN mkdir /app
RUN mkdir /app/emotes
WORKDIR /app

# Download packages
RUN apt-get update && apt-get install -y wget

COPY build/libs/*-all.jar /app/minestom-edge.jar

ENTRYPOINT ["java"]
CMD ["-jar", "/app/minestom-edge.jar"]
