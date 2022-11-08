# Overview

The frontend is written in [vue3](https://vuejs.org) in typescript. Using the [BC Parks Bootstrap theme](https://digitalspace.github.io/bcparks-bootstrap-theme/)

## Setup local development

```
- Create a .env file inside this frontend folder with the following options:

VITE_BACKEND_URL=http://localhost:3000
VITE_KEYCLOAK_URL=[keycloak authentication url for dev server]
VITE_KEYCLOAK_CLIENT_ID=[keycloak client name]
VITE_KEYCLOAK_REALM=[keycloak realm name]

- Install all requirement packages: `npm install`
- Start the application: `npm start`
```

## Technology options

**Style**:  
[Bootstrap for vue3](https://cdmoro.github.io/bootstrap-vue-3/components/Button.html)  
[Bootstrap icon for vue3](https://github.com/tommyip/bootstrap-icons-vue)  
**Unit Test**:  
[Vitest](https://vitest.dev/api/)

## Command

```
## Project setup
npm install

### Compiles and hot-reloads for development
npm run serve

### Compiles and minifies for production
npm run build

### Lints and fixes files
npm run lint
```

## Create a new project with vue

The most recent recommended way to [create a vue app](https://vuejs.org/guide/quick-start.html#with-build-tools) is using the vite, vue cli could also [create typescript app](https://vuejs.org/guide/typescript/overview.html), but some [recent note](https://vuejs.org/guide/typescript/overview.html#note-on-vue-cli-and-ts-loader) recommend to migrate over to vite

```
$ npm init vue@latest
```

## Reference reading

[Testing frameworks for vue](https://vuejs.org/guide/scaling-up/testing.html#unit-testing)
