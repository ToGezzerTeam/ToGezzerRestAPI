FROM gradle:jdk25 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar togezzer-rest-api.jar
ENTRYPOINT ["java", "-jar", "/togezzer-rest-api.jar"]