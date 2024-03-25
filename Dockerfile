FROM adoptopenjdk:11-jdk-hotspot AS build
WORKDIR /app
# cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties gradle gradlew gradlew.bat buildSrc /app/
COPY client/package.json client/build.gradle.kts /app/client/
RUN ./gradlew shadowJar --paralell --no-daemon --continue || true

COPY . /app
RUN [ "./gradlew", "shadowJar", "--parallel", "--no-daemon" ]

FROM ubuntu:latest
RUN apt-get -y update
RUN apt-get -y install git
RUN apt-get -y install openjdk-17-jdk
COPY --from=build /app/build/libs/*.jar app.jar
CMD [ "java", "-jar", "/app.jar" ]
