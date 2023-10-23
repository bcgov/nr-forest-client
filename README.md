<!-- PROJECT SHIELDS -->

<!-- [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bcgov_nr-quickstart-typescript&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=bcgov_nr-quickstart-typescript) -->

[![Merge to Main](https://github.com/bcgov/nr-forest-client/actions/workflows/merge-main.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/merge-main.yml)
[![Unit Tests and Analysis](https://github.com/bcgov/nr-forest-client/actions/workflows/unit-tests.yml/badge.svg)](https://github.com/bcgov/nr-forest-client/actions/workflows/unit-tests.yml)

[![Issues](https://img.shields.io/github/issues/bcgov/nr-forest-client)](/../../issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/bcgov/nr-forest-client)](/../../pulls)
[![MIT License](https://img.shields.io/github/license/bcgov/nr-forest-client.svg)](/LICENSE.md)
[![Lifecycle](https://img.shields.io/badge/Lifecycle-Experimental-339999)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)


The nr-forest-client is a node.js application built with [Vue.js](https://vuejs.org) in typescript as frontend, [Spring boot java](https://spring.io/projects/spring-boot#learn) as backend, postgres and oracle for database, integrated with the [greenfield-template](https://github.com/bcgov/greenfield-template) to automate the process for testing, security scanning, code quality checking, image building and deploying.

## Frontend

The frontend is built in Vue3 composition api, more info [here](frontend/README.md)

## Backend

The backend is built in Java 17 using Spring Boot 3. It is composed of multiple components, more info about the [backend here](backend/README.md), the [legacy](legacy/README.md) part that handles oracle connections and the [processor](processor/README.md) that handles the submission processing part.

## Email templates

For more info about the email templates, [click here](backend/docs/MAIL_FORMAT.md)

## Automated End-to-End tests

For automated end-to-end tests with cypress you can check [here](cypress/README.md). This is used to validate user journeys and some other cases.
