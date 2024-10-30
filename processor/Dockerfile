### Builder
FROM ghcr.io/graalvm/native-image:ol8-java17-22.3.3 AS build

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

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

# Copy
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY settings.xml.template /root/.m2/settings.xml
RUN sed -i "s|\${GITHUB_USERNAME}|${GITHUB_USERNAME}|g" /root/.m2/settings.xml && \
    sed -i "s|\${GITHUB_TOKEN}|${GITHUB_TOKEN}|g" /root/.m2/settings.xml

# Build
RUN mvn -Pnative native:compile

### Deployer
FROM gcr.io/distroless/java-base:nonroot AS deploy
ARG PORT=3100

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-processor ./nr-forest-client-processor

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:${PORT}/health | grep '"status":"UP"' || exit 1

# Startup
ENTRYPOINT ["/app/nr-forest-client-backend","--spring.profiles.active=container"]