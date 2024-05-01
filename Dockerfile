FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/*-all.jar ./Server.jar
COPY run_production/* ./

EXPOSE 4321

ENTRYPOINT [ "java", "-jar", "Server.jar" ]