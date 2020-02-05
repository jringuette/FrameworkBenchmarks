FROM quay.io/quarkus/centos-quarkus-maven:19.3.1-java11 as maven
WORKDIR /quarkus
COPY pom.xml pom.xml
COPY base/pom.xml base/pom.xml
COPY hibernate/pom.xml hibernate/pom.xml
COPY pgclient/pom.xml pgclient/pom.xml
RUN mvn dependency:go-offline -q -pl base
COPY base/src base/src
COPY hibernate/src hibernate/src
COPY pgclient/src pgclient/src
USER root
RUN chown -R quarkus /quarkus
USER quarkus

RUN mvn package -pl base -Pnative

FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /quarkus
COPY --from=maven /quarkus/base/target/*-runner benchmark
RUN chmod 775 /quarkus
CMD ["./benchmark", "-Dquarkus.http.host=0.0.0.0"]
