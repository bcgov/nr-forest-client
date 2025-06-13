FROM ghcr.io/graalvm/native-image:ol8-java17-22.3.3 AS build

# Install Maven
RUN microdnf update --nodocs -y && \
    microdnf install -y wget tar gzip && \
    wget https://dlcdn.apache.org/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz && \
    tar xzf apache-maven-3.9.10-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.10 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.9.10-bin.tar.gz && \
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
FROM gcr.io/distroless/java-base:nonroot AS deploy
ARG PORT=8080

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-backend ./nr-forest-client-backend

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK CMD curl -f http://localhost:${PORT}/actuator/health | grep '"status":"UP"'

ENV SPRING_PROFILES_ACTIVE=container

# Startup
ENTRYPOINT ["/app/nr-forest-client-backend"]
