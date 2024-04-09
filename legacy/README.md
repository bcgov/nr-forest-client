The nr-forest-client legacy backend provides the internal apis that support the frontend development and backend 
oracle communication.

## Setup local development


    For a better developer experience, use IntelliJ IDEA and configure google style as the default style.

In order to run the application, you will need an instance of postgres running, it can be local, 
can be docker or even a remote server. Just make sure you have access to the server.


Each developer can have its own set of parameters for testing and development. For this, create a file inside the 
[config folder](config) with the name **application-dev-< YOUR NAME >.yml** or 
**application-dev-< YOUR NAME >.properties** and replace the **< YOUR NAME >** with your username, 
machine name or something unique that identifies you, for example **application-dev-jsmith.yml**.

We recommend **yml** files as they tend to be less repetitive, but it's up to you which file to use. 

Also, make sure to run the application using your profile, if it's through command line, remember to pass a 
**--spring.profiles.active=dev-jsmith** argument, or if using IntelliJ, create a run configuration (more on that later).

## Configuring your yml/properties file

When creating your own configuration file, you can overwrite any of the entries contained on the main 
[configuration file](src/main/resources/application.yml) as you wish, but the most optimal way of using it is by 
setting just the parameters inside the `ca.bc.gov.nrs` context, as the rest of the parameters are already defined.

Below is an example of the database configuration:

```yml
ca:
  bc:
    gov:
      nrs:
        oracle:
          service: ${ORACLEDB_SERVICENAME:legacy}
          database: ${ORACLEDB_DATABASE:legacy}
          host: ${ORACLEDB_HOST:localhost}:${ORACLEDB_PORT:1521}
          username: ${ORACLEDB_USER:THE}
          password: ${ORACLEDB_PASSWORD:legacy}
          schema: THE
```

or using a properties file

```properties
ca.bc.gov.nrs.oracle.service=${ORACLEDB_SERVICENAME:legacy}
ca.bc.gov.nrs.oracle.database=${ORACLEDB_DATABASE:legacy}
ca.bc.gov.nrs.oracle.host=${ORACLEDB_HOST:localhost}:${ORACLEDB_PORT:1521}
ca.bc.gov.nrs.oracle.username=${ORACLEDB_USER:THE}
ca.bc.gov.nrs.oracle.password=${ORACLEDB_PASSWORD:legacy}
```

## Configuring IntelliJ Code Style

You will find a copy of the [google code style](docs/google_checks.xml) inside our [docs](docs) folder.

On the settings screen, add the checks as the following image:

[![intellij code style](docs/intellij-code-style.png)](docs/intellij-code-style.png)


## Configuring IntelliJ Run Configuration

To set your profile on IntelliJ, just run the 
[application main class](src/main/java/ca/bc/gov/app/BootApplication.java) 
and edit the configuration as the following image.

[![intellij run configuration](docs/intellij-run-config.png)](docs/intellij-run-config.png)

## Configuring Eclipse Code Style

You will find a copy of the [google code style](docs/eclipse-java-google-style.xml) inside our [docs](docs) folder.

On the window > preferences screen, go to Java > Code Style > Formatter, 
import the xml file and keep **GoogleStyle** selected as the following image:

[![eclipse code style](docs/eclipse-code-style.png)](docs/eclipse-code-style.png)


## Configuring Eclipse Run Configuration

To set your profile on Eclipse, just run the
[application main class](src/main/java/ca/bc/gov/app/LegacyApplication.java)
and edit the configuration as the following images.

[![eclipse run configuration main](docs/eclipse-run-config1.png)](docs/eclipse-run-config1.png)

[![eclipse run configuration params](docs/eclipse-run-config2.png)](docs/eclipse-run-config2.png)


## Setting up Lombok on Eclipse

If you're running eclipse, you will need to manually install lombok in order for it to work. 
The easiest way of doing that is by running any mavem command that would trigger the download of the lib, such as
`mvn clean compile`.

Once it's done, navigate to your repository folder (its usually inside your user folder, called *.m2/repository*) 
and look the latest version of lombok (inside org/projectlombok/lombok/) and run the lombok jar, 
like `java -jar lombok-X.Y.Z.jar`.

[![eclipse lombok install](docs/eclipse-lombok.png)](docs/eclipse-lombok.png)

A screen will pop up, listing all the possible IDEs, select yours and install/update it. Once it's done, 
restart eclipse.
