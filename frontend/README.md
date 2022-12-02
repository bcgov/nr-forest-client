# Overview

The frontend is written in [vue3](https://vuejs.org) composition api in typescript. Using the [BC Parks Bootstrap theme](https://digitalspace.github.io/bcparks-bootstrap-theme/)

## **Setup local development**

```
- Create a .env file inside this frontend folder with the following options:

VITE_BACKEND_URL=http://localhost:3000
VITE_KEYCLOAK_URL=[keycloak authentication url for dev server]
VITE_KEYCLOAK_CLIENT_ID=[keycloak client name]
VITE_KEYCLOAK_REALM=[keycloak realm name]

- Install all requirement packages: `npm install`
- Start the application: `npm start`
```

## **Technology options**

### **Style**:

[Bootstrap for vue3](https://cdmoro.github.io/bootstrap-vue-3/components/Button.html), installed through this [prefered installation method](https://cdmoro.github.io/bootstrap-vue-3/getting-started/#preferred-installation), so it can automatically importing components, to aviod the warn message that cannot find components when run unit tests  
[unplugin-icons for bootstrap](https://github.com/antfu/unplugin-icons)

[Bootstrap icon for vue3](https://github.com/tommyip/bootstrap-icons-vue), this doesn't work well with the bootstrap-vue-3 after installed bootstrap-vue-3 using the preferred method, so use unplugin-icons for now, until bootstrap-vue-3 adds the icons. bootstrap-icons-vue naming convention uses b-icon as prefix, so bootstrap-vue-3 will think it belongs to it, and then complains cannot found

### **Unit Test**:

[Vitest](https://vitest.dev/api/)
[Vue test util](https://test-utils.vuejs.org/api/)

### **Reusable Form Component Structure**:

#### **FormData in a json format**:

```
{
  "container_id": {
    "field_id": "value",
    "field_id": [{
      "column_id": "value",
      ...
    }],
    ...
  },
  ...
}
```

where **container_id** is whatever we'd like to use for each form section, **field_id** is for the form content in this section, for example: input, select, radio, checkbox, table, etc. **column_id** is for the fields inside a table or a group

#### **Form json schema**:

```
{
  container: {
    title: string;
    id: string;
    defaultOpen?: boolean;
    nextId?: string;
    nextText?: string;
    alwaysOpen?: boolean;
  },
  content: [{
    fieldProps: {
        label: string;
        required?: boolean;
        id: string; // unique id is required, need to be aligned with the field_id in formData
        note?: string;
        tooltip?: string;
        errorMsg?: string;
    };
    type: string;
    disabled?: boolean;
    state?: boolean;
    depend?: {
      fieldId: string;
      value: string | number | boolean;
    };
    options?: Array<CommonObjectType>; // for select, checkbox group, radio group
    addButtonText?: string; // for table
    columns?: Array<CommonObjectType>; // for table and group
  },
  ...
  ]
}
```

where the:  
**container** part has the properties for each form section, using the properties from the CollapseCard component, id need to be aliged with the one in formData
**content** part has the properties for each field in this form section

## **Command**

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

## **Create a new project with vue**

The most recent recommended way to [create a vue app](https://vuejs.org/guide/quick-start.html#with-build-tools) is using the vite, vue cli could also [create typescript app](https://vuejs.org/guide/typescript/overview.html), but some [recent note](https://vuejs.org/guide/typescript/overview.html#note-on-vue-cli-and-ts-loader) recommend to migrate over to vite

```
$ npm init vue@latest
```

## **Reference reading**

[vue3 composition api learning](https://vuejs.org/tutorial/#step-12)
[vue3 computed properties](https://vuejs.org/guide/essentials/computed.html)
[vue3 typescript with composition api](https://vuejs.org/guide/typescript/composition-api.html)
[Testing frameworks for vue](https://vuejs.org/guide/scaling-up/testing.html#unit-testing)
