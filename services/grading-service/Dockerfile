FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY services/pom.xml services/pom.xml
COPY services/grading-service/pom.xml services/grading-service/pom.xml
RUN mvn -B -ntp -f services/pom.xml -pl grading-service -am dependency:go-offline

COPY services services
RUN mvn -B -ntp -f services/pom.xml -pl grading-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/services/grading-service/target/grading-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
