FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon -x test \
    && find build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} /tmp/app.jar \;

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D -H bybusca
COPY --from=build /tmp/app.jar /app/app.jar
USER bybusca
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
