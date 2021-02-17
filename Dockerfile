FROM adoptopenjdk:8-jdk AS build
WORKDIR /app
COPY . /app
RUN ./gradlew shadowJar

FROM adoptopenjdk:8-jre
COPY --from=build /app/build/libs/*.jar app.jar
CMD [ "java", "-jar", "/app.jar" ]
