<!-- PROJECT SHIELDS -->

[![Contributors](https://img.shields.io/github/contributors/bcgov/nr-forest-client)](/../../graphs/contributors)
[![Forks](https://img.shields.io/github/forks/bcgov/nr-forest-client)](/../../network/members)
[![Stargazers](https://img.shields.io/github/stars/bcgov/nr-forest-client)](/../../stargazers)
[![Issues](https://img.shields.io/github/issues/bcgov/nr-forest-client)](/../../issues)
[![MIT License](https://img.shields.io/github/license/bcgov/nr-forest-client.svg)](/LICENSE.md)
[![Lifecycle](https://img.shields.io/badge/Lifecycle-Experimental-339999)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)

The vue-nest-template is a node.js application built with [Vue.js](https://vuejs.org) in typescript as frontend, [nestJS](https://docs.nestjs.com) as backend, postgres for database, integrated with the [greenfield-template](https://github.com/bcgov/greenfield-template) to automate the process for testing, security scanning, code quality checking, image building and deploying.

# Setup

First need to create a new reposiroty in github

## Create a New Repo Using this Branch

- `git clone https://github.com/bcgov/nr-old-growth.git [new_repo_name]` and switch to branch "vue-nest-template"
- `cd [new_repo_name]`, Remove the '.git' directory
- Re-initialize the git, `git init`
- Add the remote, `git remote add origin [new_repo_url]`

## Pipeline

- Update all the places using "**nrog**" in the yaml files under `.github/openshift` and `.github/workflows` folder, and update it to the new project name
- Add required secrets to github repo setting:
  - General secrets: 
    - OC_SERVER: openshift cluster url
    - GHCR_TOKEN: personal access token generated through user setting -> developer settings -> personal access token with the right to repo and write/delete package
  - Environment secrets:
    - Dev: OC_NAMESPACE (dev namespace), OC_TOKEN (get from openshift namespace -> administer view -> user management -> service account -> pipeline token)
    - Prod: OC_NAMESPACE (prod namespace), OC_TOKEN
    - Test: OC_NAMESPACE (test namespace), OC_TOKEN
- (Optional) If use the email service by CHES, add the CHES_SERVICE_CLIENT and CHES_CLIENT_PASSWORD to the github repo secrets as well

## Image Package

- Go the BCGov GitHub organization, search the new repo name, link the package registry to the repo, manage the access to open to public

## Frontend Local Development

- Create a .env file inside this frontend folder with the following options:

```
VITE_BACKEND_URL=http://localhost:3000
```
- (Optional) If want to enable the login authentication, add the following to the .env file as well, and uncomment the login setting in the `frontend/src/main.ts` file
```
VITE_KEYCLOAK_URL=[keycloak authentication url for dev server]
VITE_KEYCLOAK_CLIENT_ID=[keycloak client name]
VITE_KEYCLOAK_REALM=[keycloak realm name]
```
- Install all requirement packages: `npm install`
- Start the application: `npm start`

## Backend Local Development

- Create a .env file inside this backend folder with the following options (suppose there is a local postgres db to connect with):
```
  NODE_ENV=development
  POSTGRESQL_USER=[local postgres username]
  POSTGRESQL_PASSWORD=[local postgres password]
  POSTGRESQL_DATABASE=[local postgres database]
  FRONTEND_URL=[enable cors for this frontend url]
  BACKEND_URL=[enable cors for this backend url to enable try in swagger]
```
- (Optional) If want to use the email service by CHES, add the following to the .env file as well, and uncomment import in the `backend/src/app.module.ts` file
```
  EMAIL_USERNAME=[CHES service dev username]
  EMAIL_PASSWORD=[CHES service dev password]
  // CHES dev authentication url
  EMAIL_TOKEN_URL=https://dev.oidc.gov.bc.ca/auth/realms/jbd6rnxw/protocol/openid-connect/token
  // CHES dev email url
  EMAIL_POST_URL=https://ches-dev.apps.silver.devops.gov.bc.ca/api/v1/email
  EMAIL_FROM=[send from email address]
```
- Install dependencies `npm install`
- Start the server `npm start`

## Database

- There is a sample sql script file at `backend/src/databasescripts/fsa.sql`, to run the script, open pgadmin, login to the db, right click on ???Tables??? -> ???Query Tool??? -> copy the query over and run it; right click on the schemas, and refresh to get the update


## Namespace

- For new namespace, add the network policy `oc process -f .github/openshift/networkPolicies.yml | oc apply -f -`
- If require more resources, apply at the [platform registry](https://registry.developer.gov.bc.ca/)

## Set up visual studio code

**Install extensions**:  
ESlint  
Prettier  
Vetur

**Set indent and save on file**:

- Press "cmd+shift+p" and type "Preferences: Open Settings (UI)", select it
- In the open window, update the "Editor: Tab Size" to 2; search "format on save" and check the checkbox

**Enable prettier format**:  
Press "option+shift+f" and select prettier as the formatter
