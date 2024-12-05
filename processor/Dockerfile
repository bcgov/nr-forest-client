### Builder
FROM eclipse-temurin:17.0.8.1_1-jdk-jammy AS build

# Install Maven
RUN apt update -y && \
    apt install -y wget tar && \
    wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz && \
    tar xzf apache-maven-3.8.6-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.8.6 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.8.6-bin.tar.gz && \
    apt-get clean

# Add Maven to the PATH environment variable
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Receiving app version
ARG APP_VERSION=0.0.1

# Copy
WORKDIR /app
COPY pom.xml ./
COPY src ./src

# Setting app version
RUN mvn versions:set -DnewVersion=${APP_VERSION} -f pom.xml -DskipTests -Dtests.skip=true -Dskip.unit.tests=true && \
    mvn versions:commit -f pom.xml -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

# Build
RUN mvn clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

### Deployer
FROM eclipse-temurin:17.0.8.1_1-jre-jammy AS deploy
ARG PORT=3100

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-processor.jar ./nr-forest-client-processor.jar

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK CMD curl -f http://localhost:${PORT}/actuator/health | grep '"status":"UP"'

ENV SPRING_PROFILES_ACTIVE=container

# Startup
ENTRYPOINT ["java", "-jar", "/app/nr-forest-client-processor.jar"]
