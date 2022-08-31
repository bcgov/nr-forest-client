# Overview

The backend for nr-old-growth project is written in [NestJS](https://github.com/nestjs/nest) with the openapi integration.

## Setup local development

- Create a .env file inside this backend folder with the following options:

  ```
  NODE_ENV=development

  FRONTEND_URL=[enable cors for this frontend url]

  BACKEND_URL=[enable cors for this backend url to enable try in swagger]

  API_URL=[url for the forest client api]
  
  X_API_KEY=[key to access to the forest client api]

  ```

- Install dependencies `npm install`
- Start the server `npm start`
- Run test `npm run test`

## Reference reading

[Setup openapi module with nestjs](https://dev.to/arnaudcortisse/trying-out-nestjs-part-3-creating-an-openapi-document-3800)
