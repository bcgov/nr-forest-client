FROM openjdk:17.0.2@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8 as build
WORKDIR /app
COPY . ./
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

FROM eclipse-temurin:17.0.6_10-jdk-alpine@sha256:b99f9457ad5cabdfa02935d1048082dd79d318685cfa0ca79a8f623c052c406a
LABEL maintainer="Paulo Gomes da Cruz Junior <paulo.cruz@encora.com>"

WORKDIR /app

ENV LANG en_CA.UTF-8
ENV LANGUAGE en_CA.UTF-8
ENV LC_ALL en_CA.UTF-8
ENV JAVA_OPS -Xms512m -Xmx512m

COPY --from=build /app/target/nr-forest-client-legacy.jar /app/service.jar
COPY startup.sh .
COPY InstallCert.java .

RUN chmod g+w /app && \
    chmod g+x startup.sh && \
    chmod g+w ${JAVA_HOME}/lib/security/cacerts

EXPOSE 9000

HEALTHCHECK --interval=35s --timeout=4s CMD wget --spider -S http://127.0.0.1:9000/health || exit 1

# Non-privileged user
USER app

ENTRYPOINT ["sh", "startup.sh"]