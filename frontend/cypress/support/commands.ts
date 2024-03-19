/* eslint-disable no-undef */
/// <reference types="cypress" />

const generateRandomHex = (length: number): string => {
  const characters = "0123456789abcdef";
  let result = "";
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length);
    result += characters.charAt(randomIndex);
  }
  return result;
};

const jwtfy = (jwtBody: any) => btoa(JSON.stringify(jwtBody))
      .replace(/\+/g, "-")
      .replace(/\//g, "_")
      .replace(/=+$/, "");

const generateRandomUUID = (): string => {
  const parts = [
    generateRandomHex(8),
    generateRandomHex(4),
    generateRandomHex(4),
    generateRandomHex(4),
    generateRandomHex(12)
  ];
  return parts.join("-");
};

Cypress.Commands.add("addCookie", (name: string, value: string) => {
  cy.setCookie(name, value, {
    domain: "127.0.0.1",
    path: "/",
    httpOnly: false,
    secure: true,
    expiry: Date.now() + 86400000,    
  });
});

Cypress.Commands.add("expireCookie", (name: string) => {
  cy.setCookie(name, "", {
    domain: "127.0.0.1",
    path: "/",
    httpOnly: false,
    secure: false,
    expiry: Date.now() - 86400000 * 2,
  });
});

Cypress.Commands.add("addToSessionStorage", (key: string, value: any) => {
  cy.window().then((win) => {
    win.sessionStorage.setItem(key, JSON.stringify(value));
  });
});

Cypress.Commands.add("expireSessionStorage", (key: string) => {
  cy.window().then((win) => {
    win.sessionStorage.removeItem(key);
  });
});

Cypress.Commands.add("addToLocalStorage", (key: string, value: any) => {
  cy.window().then((win) => {
    win.localStorage.setItem(key, JSON.stringify(value));
  });
});

Cypress.Commands.add("expireLocalStorage", (key: string) => {
  cy.window().then((win) => {
    win.localStorage.removeItem(key);
  });
});

Cypress.Commands.add(
  "login",
  (email: string, name: string, provider: string, extra: any = "{}") => {
    cy.get(".landing-button").should("be.visible");

    const userId = generateRandomHex(32);

    const roles = provider === "idir" ? ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_ADMIN"] : ["USER"];

    const jwtBody = {
      "custom:idp_display_name": name,
      "custom:idp_name": provider,
      "custom:idp_user_id": userId,
      email,
      firstName: "UAT",
      lastName: "Test",
      "cognito:groups": roles,
      ...extra,
    };

    const accessToken = {
      "sub": generateRandomUUID(),
      "iss": `https://cognito-idp.${Cypress.env('AWS_COGNITO_REGION')}.amazonaws.com/${Cypress.env('AWS_COGNITO_REGION')}_${Cypress.env('VITE_AWS_COGNITO_POOL_ID')}`,
      "version": 2,
      "client_id": Cypress.env('VITE_AWS_COGNITO_CLIENT_ID'),
      "origin_jti": generateRandomUUID(),
      "token_use": "access",
      "scope": "openid",
      "auth_time": 1708985572,
      "exp": 2908989880,
      "iat": 2908989581,
      "jti": generateRandomUUID(),
      "username": userId
    };

    const baseCookieName = `CognitoIdentityServiceProvider.${Cypress.env('AWS_COGNITO_CLIENT_ID')}`;
    const baseUserCookieName = `${baseCookieName}.${userId}`;
    const cognitoResponse = {
      "AccessToken": `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${jwtfy(accessToken)}.`,
      "ExpiresIn": 300,
      "IdToken": `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${jwtfy(jwtBody)}.`,
      "TokenType": "Bearer"
    };


    cy.intercept(
      "POST", 
      `https://cognito-idp.${Cypress.env('AWS_COGNITO_REGION')}.amazonaws.com/`,
      { statusCode: 200, body: cognitoResponse, }
    ).as("cognitoPull");

    cy.addCookie(`${baseCookieName}.LastAuthUser`,userId);    
    cy.addCookie(`${baseUserCookieName}.idToken`,`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${jwtfy(jwtBody)}.`);
    
    cy.reload();
    cy.wait(1000);
  }
);

Cypress.Commands.add("logout", () => {
  cy.get("[data-id=logout-btn]").should("be.visible");
  cy.expireCookie("idToken");
  cy.reload();
  cy.wait(1000);
});

Cypress.Commands.add("getMany", (names: string[]): Cypress.Chainable<any[]> => {
  const values: any[] = [];

  for (const arg of names) {
    cy.get(arg).then((value) => values.push(value));
  }

  return cy.wrap(values);
});
