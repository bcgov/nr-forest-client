FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Paulo Gomes da Cruz Junior <paulo.cruz@encora.com>"

WORKDIR /usr/share/service/
RUN mkdir -p /usr/share/service/config &&\
    mkdir -p /usr/share/service/dump &&\
    mkdir -p /usr/share/service/public

ENV LANG en_CA.UTF-8
ENV LANGUAGE en_CA.UTF-8
ENV LC_ALL en_CA.UTF-8

COPY ./nr-forest-client-backend.jar /usr/share/service/service.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/share/service/service.jar"]

