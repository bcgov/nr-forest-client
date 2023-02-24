FROM openjdk:17 as build
WORKDIR /app
COPY . ./
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true -q
RUN echo

FROM eclipse-temurin:17.0.6_10-jre-alpine@sha256:f59c1acc26975859545eabb2051f4b9a41d5ef278aad9dfe42bdb0aff5611613
LABEL maintainer="Paulo Gomes da Cruz Junior <paulo.cruz@encora.com>"

WORKDIR /usr/share/service/
RUN mkdir -p /usr/share/service/config &&\
    mkdir -p /usr/share/service/dump &&\
    mkdir -p /usr/share/service/public

ENV LANG en_CA.UTF-8
ENV LANGUAGE en_CA.UTF-8
ENV LC_ALL en_CA.UTF-8

COPY --from=build /app/target/nr-forest-client-backend.jar /usr/share/service/service.jar

EXPOSE 3000

HEALTHCHECK --interval=35s --timeout=4s CMD wget --spider -S http://127.0.0.1:3000/health || exit 1

# Non-privileged user
USER service

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/share/service/service.jar"]