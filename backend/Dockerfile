FROM openjdk:17.0.2@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8 AS build
WORKDIR /app
COPY . ./
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true -q
RUN echo

FROM eclipse-temurin:17.0.7_7-jre-alpine@sha256:8a139831f95086a107f510f239cb311d4196b94f0f15192a22f8f7a43f343d02
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