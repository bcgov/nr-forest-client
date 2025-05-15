### Builder
FROM ghcr.io/graalvm/native-image:ol8-java17-22.3.3@sha256:d55ebb6eec6751b87ce9d34ab1004fa088d227d90d3edb4d9b3a994e54ac285a AS build

# Install Maven
RUN microdnf update --nodocs -y && \
    microdnf install -y wget tar && \
    wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz && \
    tar xzf apache-maven-3.8.6-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.8.6 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.8.6-bin.tar.gz && \
    microdnf clean all

# Add Maven to the PATH environment variable
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Copy
WORKDIR /app
COPY pom.xml ./
COPY src ./src

# Build
RUN mvn -Pnative native:compile

### Deployer
FROM gcr.io/distroless/java-base:nonroot@sha256:71ca0e469a097f72e74edceb9d7e2951a1fb6b44c851b6cc482c1c86408a947e AS deploy
ARG PORT=9000

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-legacy ./nr-forest-client-legacy

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK CMD curl -f http://localhost:${PORT}/actuator/health | grep '"status":"UP"'

ENV SPRING_PROFILES_ACTIVE=container

# Startup
ENTRYPOINT ["/app/nr-forest-client-legacy"]
