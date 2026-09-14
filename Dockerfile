FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY backend/pom.xml backend/pom.xml

RUN mvn -f backend/pom.xml dependency:go-offline -DskipTests

COPY backend/src backend/src

RUN mvn -f backend/pom.xml clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/wms-backend.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]