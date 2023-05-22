FROM adoptopenjdk:11-jdk-hotspot AS build
WORKDIR /app
# cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties gradle gradlew gradlew.bat buildSrc /app/
COPY client/package.json client/build.gradle.kts /app/client/
RUN ./gradlew shadowJar --paralell --no-daemon --continue || true

COPY . /app
RUN [ "./gradlew", "shadowJar", "--parallel", "--no-daemon" ]

FROM adoptopenjdk:11-jre-hotspot
COPY --from=build /app/build/libs/*.jar app.jar
CMD [ "java", "-jar", "/app.jar" ]
