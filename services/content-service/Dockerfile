FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY libs/common-dtos/pom.xml libs/common-dtos/pom.xml
COPY services/pom.xml services/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/content-service/pom.xml services/content-service/pom.xml
COPY services/rag-service/pom.xml services/rag-service/pom.xml
COPY services/exam-service/pom.xml services/exam-service/pom.xml
COPY services/grading-service/pom.xml services/grading-service/pom.xml
COPY services/integrity-service/pom.xml services/integrity-service/pom.xml
COPY services/llm-orchestrator/pom.xml services/llm-orchestrator/pom.xml
RUN mvn -B -ntp -f pom.xml -pl services/content-service,libs/common-dtos -am dependency:go-offline

COPY libs libs
COPY services services
RUN mvn -B -ntp -f pom.xml -pl services/content-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/services/content-service/target/content-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
