FROM gradle:8.5.0-jdk17 AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN GITHUB_ACTOR=${GITHUB_ACTOR} GITHUB_TOKEN=${GITHUB_TOKEN} gradle assemble
FROM eclipse-temurin:17-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/runner-service.jar
COPY newrelic/ /app/newrelic/
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "/app/runner-service.jar"]