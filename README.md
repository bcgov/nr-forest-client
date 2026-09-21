<!-- PROJECT SHIELDS -->
<div align="center">

# Forest Client Management System (FCMS)

[![Merge to Main](https://github.com/bcgov/nr-forest-client/actions/workflows/merge.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/merge.yml)
[![Unit Tests and Analysis](https://github.com/bcgov/nr-forest-client/actions/workflows/analysis.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/analysis.yml)
[![Issues](https://img.shields.io/github/issues/bcgov/nr-forest-client)](https://github.com/bcgov/nr-forest-client/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/bcgov/nr-forest-client)](https://github.com/bcgov/nr-forest-client/pulls)
[![Apache 2.0 License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Lifecycle](https://img.shields.io/badge/Lifecycle-Maturing-007EC6)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)

<p>The official client registry and identity management platform for the Province of British Columbia's Ministry of Forests.</p>

[Wiki Documentation](https://github.com/bcgov/nr-forest-client/wiki) • [Database Schema (SchemaSpy)](https://bcgov.github.io/nr-forest-client/) • [Entity-Relationship Diagram](https://bcgov.github.io/nr-forest-client/nrfc/relationships.html) • [Report Bug](https://github.com/bcgov/nr-forest-client/issues)

</div>

---

## Table of Contents

- [Overview](#overview)
  - [User Groups and Roles](#user-groups-and-roles)
  - [Modernization Vision](#modernization-vision)
- [Architecture](#architecture)
  - [System Context & Data Flow](#system-context--data-flow)
- [Technology Stack](#technology-stack)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Infrastructure Services (Docker Compose)](#1-infrastructure-services-docker-compose)
  - [2. Frontend Development](#2-frontend-development)
  - [3. Backend Services](#3-backend-services)
- [Database Management](#database-management)
  - [Flyway Migrations](#flyway-migrations)
  - [SchemaSpy Documentation](#schemaspy-documentation)
- [Testing Strategy](#testing-strategy)
  - [Frontend Tests (Vitest)](#frontend-tests-vitest)
  - [Backend Tests (JUnit & Testcontainers)](#backend-tests-junit--testcontainers)
  - [End-to-End User Journey Tests (Cypress & Cucumber)](#end-to-end-user-journey-tests-cypress--cucumber)
- [CI/CD & Deployment](#cicd--deployment)
- [Documentation & Wiki](#documentation--wiki)
- [Contributing & Conventions](#contributing--conventions)
- [License](#license)

---

## Overview

**Forest Client** is the authoritative repository of client information for the British Columbia Ministry of Forests. The ministry relies on Forest Client to establish, verify, and administer business relationships with corporations, First Nations, and individuals conducting business with the province or operating under the [*Forest Act*](https://www.bclaws.gov.bc.ca/civix/document/id/complete/statreg/96157_00).

### User Groups and Roles

The application caters to two primary user audiences:

1. **External Clients**:
   - **BCeID Business Users**: Authorized representatives of incorporated businesses, partnerships, and sole proprietorships applying for new client registrations or associating corporate locations.
   - **BC Services Card (BCSC) Users**: Individuals applying for client registration for individual permits or timber mark operations.
2. **Ministry Staff (IDIR)**:
   - **Client Viewer**: Read-only search, viewing client details, locations, contacts, and historical audit logs.
   - **Client Editor**: Authoring and updating client details, adding new locations and contacts, creating new client records.
   - **Client Reviewer / Admin**: Reviewing pending external submissions, matching against legacy records, adjudicating approvals or rejections, and managing client status (suspension, deactivation, amalgamations).

### Modernization Vision

- **Single Source of Truth**: Serve as the central, modernized web-based client registry across natural resource ministries.
- **Self-Service Onboarding**: Enable external applicants to submit complete client registration requests online.
- **Automated Validation & Duplicate Detection**: Cross-reference submissions against BC Registry corporate data and legacy databases to prevent duplicates and expedite approvals.
- **Bi-Directional Legacy Sync**: Synchronize modern PostgreSQL transactions with legacy Oracle databases (Forest Tenures / FTA and RESULTS integration).

---

## Architecture

Forest Client is architected as a cloud-native, multi-tier system deployed on BC Government OpenShift (Kubernetes) Silver clusters.

### System Context & Data Flow

```mermaid
flowchart TD
    subgraph Users["User Layer"]
        ExtUser["External User\n(BCeID / BC Services Card)"]
        StaffUser["Ministry Staff\n(IDIR)"]
    end

    subgraph Ingress["Ingress & Security"]
        Router["OpenShift Route / Ingress"]
        FAM["FAM / AWS Cognito\n(Authentication & JWT)"]
    end

    subgraph FrontendApp["Frontend Application (SPA)"]
        VueApp["Vue 3 + TypeScript\n(Vite + Carbon Design)"]
    end

    subgraph BackendServices["Backend Services Layer"]
        BE["Backend API (WebFlux)\nSpring Boot 4 / Java 17\nGraalVM Native Image"]
        PROC["Processor Service\nSpring Integration\nChannel #1-#10 Engine"]
        LEGACY["Legacy Connector API\nSpring Boot 4 / Java 21\nOracle DB Adapter"]
    end

    subgraph DataStorage["Data Storage Layer"]
        PG[("PostgreSQL 13 / 17\n(Primary Store)\nFlyway Migrated")]
        ORACLE[("Legacy Oracle DB\n(THE Schema)\nFlyway Migrated")]
    end

    subgraph ExternalAPIs["External Services"]
        BCREG["BC Registry API\n(Corporate Lookup)"]
        CPOST["Canada Post\n(AddressComplete)"]
        CHES["CHES\n(Common Hosted Email)"]
    end

    ExtUser -->|Access Application| Router
    StaffUser -->|Access Application| Router
    Router --> VueApp

    VueApp -->|Federated SSO| FAM
    VueApp -->|Bearer JWT Requests| BE

    BE -->|Read / Write Submissions| PG
    BE -->|Verify Business| BCREG
    BE -->|Resolve Addresses| CPOST
    BE -->|Trigger Notifications| CHES
    BE -->|Proxy Legacy Queries| LEGACY

    LEGACY -->|Query / Sync Records| ORACLE

    PROC -->|Poll Submissions (#1)| PG
    PROC -->|Verify Duplicates (#2)| LEGACY
    PROC -->|Auto-Approve (#4)| PG
    PROC -->|Persist Approved (#10)| LEGACY
    PROC -->|Trigger Emails (#5, #8, #9)| BE
```

---

## Technology Stack

| Layer / Component | Technology | Version | Purpose |
|---|---|---|---|
| **Frontend** | Vue 3 (Composition API) | 3.5.x | Reactive single-page web application |
| **Frontend Tooling** | Vite, TypeScript, Sass | 8.3.x / ~6.0.0 / ~1.104.1 | Lightning-fast build, typed modules, styling |
| **UI Components** | Carbon Design System | `@carbon/web-components` 2.x | Accessible BC Gov-aligned UI system |
| **Backend API** | Spring Boot (WebFlux), Java | 4.1.x / Java 17 | Reactive non-blocking REST API, GraalVM native |
| **Background Processor** | Spring Integration, Java | 4.1.x / Java 17 | Async submission pipeline with queue channels |
| **Legacy Connector** | Spring Boot (WebFlux), Java | 4.1.x / Java 21 | High-throughput reactive Oracle database interface |
| **Primary Database** | PostgreSQL | 13.x (moving to 17) | Modern relational store with R2DBC |
| **Legacy Database** | Oracle Database Free | 23.x (compatible with 19c) | Ministry enterprise data store (`THE` schema) |
| **Database Migrations**| Flyway | 10.x / 11.x | Versioned, reproducible SQL schema migrations |
| **Testing: Frontend** | Vitest, Vue Test Utils | 5.x / 2.x | Fast, modern unit and component testing |
| **Testing: Backend** | JUnit 5/6, Mockito, Testcontainers | 6.x / 5.x / 2.x | Unit and containerized integration testing |
| **Testing: E2E** | Cypress, Cucumber Gherkin | 16.x / 5.x | BDD user journey automation and regression |

---

## Repository Structure

```text
nr-forest-client/
├── frontend/               # Vue 3 SPA frontend source, components, and tests
├── backend/                # Main Spring Boot reactive backend API
├── legacy/                 # Spring Boot legacy service connecting to Oracle
├── processor/              # Spring Integration background processing engine
├── cypress/                # Cypress End-to-End user journey test suite (Gherkin/BDD)
├── database/               # PostgreSQL Dockerfile and configuration
├── .github/                # GitHub Actions CI/CD workflows and issue templates
├── docker-compose.yml      # Local development container orchestration
└── README.md               # Project overview and guide (this document)
```

---

## Getting Started

### Prerequisites

Ensure you have the following tools installed:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or [Podman](https://podman.io/)
- [Java Development Kit (JDK)](https://adoptium.net/) 17 or 21
- [Apache Maven](https://maven.apache.org/) 3.9+
- [Node.js](https://nodejs.org/) (v22 or v24 LTS) and [npm](https://www.npmjs.com/)

---

### 1. Infrastructure Services (Docker Compose)

Start the local PostgreSQL, Oracle Free, and Flyway migration services:

```bash
docker compose up -d database legacydb legacyflyway
```

- **PostgreSQL**: Accessible at `localhost:5432` (`user: postgres`, `password: default`, `database: postgres`)
- **Oracle Database**: Accessible at `localhost:1521` (`user: THE`, `password: default`, `service: FREEPDB1`)
- **Flyway**: Automatically executes migration scripts from `legacy/src/test/resources/db/migration` into the Oracle database and exits when finished.

---

### 2. Frontend Development

Navigate to the `frontend/` directory:

```bash
cd frontend
```

#### Option A: Running with Live Backend

Create a `.env` file in `frontend/`:

```env
VITE_BACKEND_URL=http://localhost:8080
VITE_FRONTEND_URL=http://localhost:3000
VITE_NODE_ENV=openshift-dev
VITE_COVERAGE=true
```

Install dependencies and start the development server:

```bash
npm install
npm start
```

The application will be available at `http://localhost:3000`.

#### Option B: Standalone Mode with WireMock Stubs

If you want to work on frontend UI/UX without running Java backend services, use the embedded WireMock stub server:

```bash
# Starts both the WireMock stubs and Vite in preview mode
# Starts both the WireMock stubs and the Vite development server

# Or run the stub server independently in a dedicated terminal
npm run stub
```

---

### 3. Backend Services

Each backend service (`backend/`, `legacy/`, and `processor/`) supports personal developer profiles to override settings without committing secrets. Create an `application-dev-<yourname>.yml` file in the `config/` folder of the service you are running.

> [!NOTE]
> The examples below use the safe local container defaults defined in `docker-compose.yml` (`postgres/default` and `THE/default`). Never commit production credentials, secret tokens, or private network hosts to Git configuration files.

#### A. Main Backend API (`backend/config/application-dev-<yourname>.yml`)

```yml
ca:
  bc:
    gov:
      nrs:
        # Local PostgreSQL container (docker-compose: database)
        postgres:
          host: localhost:5432
          database: postgres
          username: postgres
          password: default
        # Frontend URL for CORS
        frontend:
          url: http://localhost:3000
        # Legacy Oracle service endpoint
        legacy:
          url: http://localhost:9000
```

#### B. Legacy Oracle Service (`legacy/config/application-dev-<yourname>.yml`)

```yml
# Override TCPS/SSL with standard TCP R2DBC URL to connect to the local container
spring:
  r2dbc:
    url: r2dbc:oracle://${ca.bc.gov.nrs.oracle.host}:${ca.bc.gov.nrs.oracle.port}/${ca.bc.gov.nrs.oracle.service}

ca:
  bc:
    gov:
      nrs:
        # Local Oracle container (docker-compose: legacydb)
        oracle:
          host: localhost
          port: 1521
          service: FREEPDB1
          database: FREEPDB1
          schema: THE
          username: THE
          password: default
```

#### C. Background Processor (`processor/config/application-dev-<yourname>.yml`)

```yml
ca:
  bc:
    gov:
      nrs:
        # Local PostgreSQL container (docker-compose: database)
        postgres:
          host: localhost:5432
          database: postgres
          username: postgres
          password: default
        # Main Backend API endpoint
        backend:
          uri: http://localhost:8080/api
        # Legacy Oracle service endpoint
        legacy:
          uri: http://localhost:9000/api
```

#### Running the Services

Run each service from its respective directory specifying your active profile:

```bash
# Main Backend API (runs on port 8080)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev-<yourname>

# Legacy Oracle Service (runs on port 9000)
cd legacy
mvn spring-boot:run -Dspring-boot.run.profiles=dev-<yourname>

# Processor Service (runs on port 3100)
cd processor
mvn spring-boot:run -Dspring-boot.run.profiles=dev-<yourname>
```

---

## Database Management

### Flyway Migrations

Database structures are version-controlled using [Flyway](https://documentation.red-gate.com/flyway):

- **PostgreSQL Migrations**: Managed in [`backend/src/main/resources/db/migration/`](backend/src/main/resources/db/migration/). The Spring Boot backend automatically applies pending SQL scripts on startup.
- **Oracle Migrations**: Maintained in [`legacy/src/test/resources/db/migration/`](legacy/src/test/resources/db/migration/) and applied automatically via the `legacyflyway` container.

### SchemaSpy Documentation

We publish automated Entity-Relationship diagrams and database schema references generated with [SchemaSpy](https://schemaspy.org/):

- **Interactive Relationships Diagram**: [https://bcgov.github.io/nr-forest-client/nrfc/relationships.html](https://bcgov.github.io/nr-forest-client/nrfc/relationships.html)
- **Full Database Documentation**: [https://bcgov.github.io/nr-forest-client/](https://bcgov.github.io/nr-forest-client/)

---

## Testing Strategy

All contributions require thorough automated test verification. We enforce a minimum threshold of **80% test coverage** for new and modified logic.

### Frontend Tests (Vitest)

```bash
cd frontend
npm run test:unit     # Run unit tests (with Vitest coverage)
npm run coverage      # Run full suite (unit, component, and e2e coverage)
```

### Backend Tests (JUnit & Testcontainers)

```bash
cd backend
mvn clean verify -P all-tests        # Run unit tests and Testcontainers integration tests

cd legacy
mvn clean test

cd processor
mvn clean test
```

### End-to-End User Journey Tests (Cypress & Cucumber)

End-to-End tests reside in [`cypress/`](cypress/) and are authored in plain English using **Cucumber Gherkin BDD** (`.feature` files):

```bash
cd cypress
npm install

# Interactive Cypress GUI (headed)
npm run cy:open -- --config baseUrl=http://localhost:3000

# Headless CI Execution
npm run cy:run -- --config baseUrl=http://localhost:3000
```

> [!TIP]
> **Community-Driven Test Cases**: Anyone can propose a new user test journey by opening a GitHub Issue with the **"User provided automated test-case"** template. Our [`issue-gherkin.yml`](.github/workflows/issue-gherkin.yml) GitHub Action automatically compiles the issue into an executable `.feature` scenario!

---

## CI/CD & Deployment

- **Pull Request Validation**: Every PR triggers [`.github/workflows/analysis.yml`](.github/workflows/analysis.yml), running linter checks, frontend Vitest tests, backend Maven builds, and SonarCloud quality gate analysis.
- **Ephemeral PR Environments**: Pull requests deploy automated preview environments via GitHub Actions to test changes in isolation.
- **Continuous Deployment**: Merges to `main` that include non-documentation changes trigger [`.github/workflows/merge.yml`](.github/workflows/merge.yml), building container images and deploying to OpenShift Silver dev/test clusters.

---

## Documentation & Wiki

For deep-dive architecture notes, API specifications, and team frameworks, explore our [GitHub Wiki](https://github.com/bcgov/nr-forest-client/wiki):

- [Architecture & System Design](https://github.com/bcgov/nr-forest-client/wiki/ArchitectureIndex)
  - [Frontend Architecture](https://github.com/bcgov/nr-forest-client/wiki/Frontend-Architecture-Overview)
  - [Backend Architecture](https://github.com/bcgov/nr-forest-client/wiki/Backend-Architecture-Overview)
  - [Processor Workflow & Channels](https://github.com/bcgov/nr-forest-client/wiki/Processor-Architecture-Overview)
  - [Legacy Oracle Connector](https://github.com/bcgov/nr-forest-client/wiki/Legacy-Architecture-Overview)
- [Developer Guides & Setup](https://github.com/bcgov/nr-forest-client/wiki/DevelopmentIndex)
  - [Docker & Infra Setup](https://github.com/bcgov/nr-forest-client/wiki/Docker)
  - [Frontend Local Setup](https://github.com/bcgov/nr-forest-client/wiki/Frontend-Setup)
  - [Backend Local Setup](https://github.com/bcgov/nr-forest-client/wiki/Backend-Setup)
  - [Cypress BDD Guide](https://github.com/bcgov/nr-forest-client/wiki/Cypress-E2E-Testing)
- [Team Delivery Framework](https://github.com/bcgov/nr-forest-client/wiki/Team-Info)
  - Scrum ceremonies, Story points (Fibonacci scale), Definition of Done, and the Developer Contract.
- [Knowledge Base & Integrations](https://github.com/bcgov/nr-forest-client/wiki/KnowledgeBaseIndex)
  - FAM (AWS Cognito SSO), BC Registry, Canada Post, and CHES Email Service.

---

## Contributing & Conventions

We welcome contributions! Please review our conventions before submitting a pull request:

- **Conventional Commits**: Format commit messages as `feat(...)`, `fix(...)`, `docs(...)`, `chore(...)`, or `test(...)`.
- **Branch Naming**: Use `feature/fe/<issue-name>`, `fix/be/<issue-name>`, `chore/deps/...`.
- **Code Reviews**: All PRs require passing automated CI checks, test coverage compliance, and at least one approving review from the core team.

---

## License

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.
