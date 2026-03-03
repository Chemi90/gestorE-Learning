FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY services/pom.xml services/pom.xml
COPY services/integrity-service/pom.xml services/integrity-service/pom.xml
RUN mvn -B -ntp -f services/pom.xml -pl integrity-service -am dependency:go-offline

COPY services services
RUN mvn -B -ntp -f services/pom.xml -pl integrity-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/services/integrity-service/target/integrity-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
