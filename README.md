<!-- PROJECT SHIELDS -->

<!-- [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bcgov_nr-quickstart-typescript&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=bcgov_nr-quickstart-typescript) -->

[![Merge to Main](https://github.com/bcgov/nr-forest-client/actions/workflows/merge.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/merge.yml)
[![Unit Tests and Analysis](https://github.com/bcgov/nr-forest-client/actions/workflows/analysis.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/analysis.yml)

[![Issues](https://img.shields.io/github/issues/bcgov/nr-forest-client)](/../../issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/bcgov/nr-forest-client)](/../../pulls)
[![MIT License](https://img.shields.io/github/license/bcgov/nr-forest-client.svg)](/LICENSE.md)
[![Lifecycle](https://img.shields.io/badge/Lifecycle-Experimental-339999)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)


The nr-forest-client is a node.js application built with [Vue.js](https://vuejs.org) in typescript as frontend, [Spring boot java](https://spring.io/projects/spring-boot#learn) as backend, postgres and oracle for database, integrated with the [QuickStart for OpenShift](https://github.com/bcgov/quickstart-openshift) to automate the process for testing, security scanning, code quality checking, image building and deploying.

## Frontend

The frontend is built in Vue3 composition api, more info [here](frontend/README.md)

## Backend

The backend is built in Java 17 using Spring Boot 3. It is composed of multiple components, more info about the [backend here](backend/README.md), the [legacy](legacy/README.md) part that handles oracle connections and the [processor](processor/README.md) that handles the submission processing part.

## Email templates

For more info about the email templates, [click here](backend/docs/MAIL_FORMAT.md)

## Automated End-to-End tests

For automated end-to-end tests with cypress you can check [here](cypress/README.md). This is used to validate user journeys and some other cases.


## Database Schema documentation

As the application evolves, so does the data, and to control the evolution of the database that stores all this new data. To make it in a controllable way, we can apply the same strategy we use for our codebase into our database. To achieve that, we can use tools that will version and apply changes into the database in a controlled way. Team Alliance decided to make use of [Flyway](https://documentation.red-gate.com/flyway) as the de-facto tool to database versioning. This means that we can recreate the database structure as many times as we need, along with basic data that needs to be inserted into the database.

We have the database schema documented using [SchemaSpy](https://schemaspy.org/). This is particularly useful tool for database administrators, developers, and analysts who need to understand the structure and relationships within a database. This allow us to automatically generate a visualization of the database schema through Entity-Relationship (ER) diagrams. These diagrams help users quickly grasp the relationships between tables, making it easier to navigate and understand complex databases. 

You can find our database diagram on [this page](https://bcgov.github.io/nr-forest-client/nrfc/relationships.html) and more information about the database on [this page](https://bcgov.github.io/nr-forest-client/)
