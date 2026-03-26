FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiamos Maven porque tu repo NO tiene mvnw
RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src

RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

