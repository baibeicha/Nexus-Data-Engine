FROM gradle:8.4-jdk21 AS builder

ENV GRADLE_USER_HOME /cache
VOLUME /cache

WORKDIR /app
COPY build.gradle ./
COPY src src

ONBUILD RUN if [ -f settings.gradle ]; then COPY settings.gradle ./; fi

RUN gradle dependencies --no-daemon
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]