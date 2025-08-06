FROM eclipse-temurin:24-jre-alpine

# Download packages
RUN apk add --no-cache wget

RUN mkdir /app
RUN mkdir /app/emotes
WORKDIR /app

COPY build/libs/*-all.jar /app/minestom-edge.jar

ENTRYPOINT ["java"]
CMD ["-jar", "/app/minestom-edge.jar"]
