FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY services/pom.xml services/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
RUN mvn -B -ntp -f services/pom.xml -pl api-gateway -am dependency:go-offline

COPY services services
RUN mvn -B -ntp -f services/pom.xml -pl api-gateway -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/services/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
