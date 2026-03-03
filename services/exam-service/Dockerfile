FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY services/pom.xml services/pom.xml
COPY services/exam-service/pom.xml services/exam-service/pom.xml
RUN mvn -B -ntp -f services/pom.xml -pl exam-service -am dependency:go-offline

COPY services services
RUN mvn -B -ntp -f services/pom.xml -pl exam-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/services/exam-service/target/exam-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
