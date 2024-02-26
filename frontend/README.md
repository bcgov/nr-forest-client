# Overview

The frontend is written in [vue3](https://vuejs.org) composition api in typescript, building with [Vite](https://vitejs.dev). Using the [BC Parks Bootstrap theme](https://digitalspace.github.io/bcparks-bootstrap-theme/). The theme still need to be discussed with UX designers.

## **Setup local development**

- Create a .env file inside this frontend folder with the following options:

  ```
  # Location of the backend url
  VITE_BACKEND_URL=http://localhost:8080
  # Used by CORS, must use the same port as your vite
  VITE_FRONTEND_URL=http://localhost:3000
  # Application environment
  VITE_NODE_ENV=openshift-dev
  # Used only on dev and build, to enable coverage
  VITE_COVERAGE=true

  ```

- Install all requirement packages: `npm install`
- Start the application: `npm start`
- Run test: `npm test`

### **Running with Stubs**

If you don't want to rely on the backend for development (maybe you're just doing cosmetic changes), you can run the application with the stubs,
for that you can run `npm run preview` and that will spin-up the stub server for you.

For more information check the [stubs](#stubs) section.

### **Feature Flags**

We introduced feature flags to the application and it can be controlled by adding `VITE_FEATURE_FLAGS` property to your environment. The value should be a JSON stringified that will be consumed and are available as a global value `$features`.

- BCEID_MULTI_ADDRESS: enables multiple addresses. When this is turned off, every contact is automatically associated to the MAILING ADDRESS.

### **Authentication**

Authentication is provided by FAM through our backend, and can be consumed both through `$session` global value or by importing `ForestClientUserSession` from `@/helpers/ForestClientUserSession`

## **Setup visual studio code**

### **Install extensions**:

Prettier Formatter:
[![vscode prettier formatter](public/docs/vscode-extension-prettier.png)](public/docs/vscode-extension-prettier.png)

Volar [as recommended](https://vuejs.org/guide/typescript/overview.html#ide-support):
[![vscode volar](public/docs/vscode-extension-volar.png)](public/docs/vscode-extension-volar.png)

### **Set indent and save on file**:

- Press "cmd+shift+p" and type "Preferences: Open Settings (UI)", select it
- In the open window, update the "Editor: Tab Size" to 2; search "format on save" and check the checkbox

### **Enable prettier format**:

Press "option+shift+f" and select prettier as the formatter

## **Technology options**

## **Stubs**:

To speed-up development and allow a simple development environment, a stub server was added to fake backend responses. For this we use the same technology as we use
in our backend, that is [wiremock](https://wiremock.org/). For that run `npm run stub` in a separated window to execute the backend server. You can add new responses or update
the existing ones by tweeking the content of the [stub](stub/) folder following the [response templating](https://wiremock.org/docs/response-templating/) format and the [request matching](https://wiremock.org/docs/request-matching/) to suit your needs.

### **Style**:

Using Carbon

### **Test**:

[Vitest](https://vitest.dev/api/)  
[Vue test util](https://test-utils.vuejs.org/api/)
[Cypress](https://www.cypress.io/)

### **Vite vs vue cli**

The most recent recommended way to [create a vue app](https://vuejs.org/guide/quick-start.html#with-build-tools) is using the vite, vue cli could also [create typescript app](https://vuejs.org/guide/typescript/overview.html), but some [recent note](https://vuejs.org/guide/typescript/overview.html#note-on-vue-cli-and-ts-loader) recommends to migrate over to vite.  
The Vue CLI is built on top of webpack, it is a module bundler that will bundle your entire Vue project on startup, hot reloads, and compilation. Vue Vite instead offers faster startup, hot reload, and compilation speeds than bundler-based solutions during development.

## **Reference reading**

[vue3 composition api learning](https://vuejs.org/tutorial/#step-12)  
[vue3 computed properties](https://vuejs.org/guide/essentials/computed.html)  
[vue3 typescript with composition api](https://vuejs.org/guide/typescript/composition-api.html)  
[Testing frameworks for vue](https://vuejs.org/guide/scaling-up/testing.html#unit-testing)
