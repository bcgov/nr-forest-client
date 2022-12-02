The nr-forest-client backend provides the internal apis that support the frontend development.

## Setup local development

```
- Export env variables for database credentials:

```

export ORACLEDB_HOST=[]
export ORACLEDB_PORT=[]
export ORACLEDB_SERVICENAME=[]
export ORACLEDB_USER=[]
export ORACLEDB_PASSWORD=[]

export POSTGRESQL_HOST=[]
export POSTGRESQL_DATABASE=[]
export POSTGRESQL_USER=[]
export POSTGRESQL_PASSWORD=[]

export FRONTEND_URL="http://localhost:8080"

export EMAIL_USERNAME=[CHES_EMAIL_USERNAME]
export EMAIL_PASSWORD=[CHES_EMAIL_PASSWORD]
export EMAIL_TOKEN_URL=[URL_TO_GET_CHES_EMAIL_TOKEN]
export EMAIL_API_URL=[CHES_EMAIL_API]

```
- Start the application: `./mvnw spring-boot:run`
- Rebuild the application if need: `./mvnw clean package`
- Run test: `./mvnw test`
```
