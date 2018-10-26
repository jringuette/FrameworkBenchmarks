FROM gradle:4.7.0-jdk8 as gradle
USER root
WORKDIR /open-liberty
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src
RUN gradle libertyPackage

FROM openjdk:8-jre-slim
WORKDIR /open-liberty
COPY --from=gradle /open-liberty/build/OpenLibertyBenchmark.jar app.jar
CMD ["java", "-server", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-jar", "app.jar"]
