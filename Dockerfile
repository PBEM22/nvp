FROM gradle:8.5-jdk21-focal AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-focal
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
