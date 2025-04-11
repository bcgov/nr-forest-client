import { version } from "../package.json";

export const checkEnv = (key: string) =>
  window.localStorage.getItem(key) || import.meta.env[key];

export const featureFlags: Record<string, any> = JSON.parse(
  checkEnv("VITE_FEATURE_FLAGS") || "{}"
);
export const backendUrl: string = checkEnv("VITE_BACKEND_URL");
export const frontendUrl: string = checkEnv("VITE_FRONTEND_URL");
export const greenDomain: string = checkEnv("VITE_GREEN_DOMAIN");
export const nodeEnv: string = checkEnv("VITE_NODE_ENV");

export const appVersion: string = version;

export const cognitoRegion: string = checkEnv("VITE_AWS_COGNITO_REGION");
export const cognitoDomain: string = checkEnv("VITE_AWS_COGNITO_DOMAIN");
export const cognitoClientId: string = checkEnv("VITE_AWS_COGNITO_CLIENT_ID");
export const cognitoPoolId: string = checkEnv("VITE_AWS_COGNITO_POOL_ID");
export const cognitoLogoutChainUrl: string = checkEnv(
  "VITE_AWS_COGNITO_LOGOUT_CHAIN_URL"
);
export const cognitoEnvironment: string = checkEnv(
  "VITE_AWS_COGNITO_ENVIRONMENT"
);

const zone = cognitoEnvironment.toLowerCase();

const getReturlHost = (): string => {
  let host = "";
  if (zone !== "prod") {
    host = `${zone}.`;
  }
  host += "loginproxy.gov.bc.ca";
  return host;
};

const returlHost = getReturlHost();

const getSignOutHost = (): string => {
  const hostname = zone === "prod" ? "logon7" : "logontest7";
  return `https://${hostname}.gov.bc.ca`;
};

const signOutHost = getSignOutHost();

const getProviderRedirectUri = (provider: string) => {
  let uri = `${frontendUrl}/landing`;
  if (["bceidbusiness", "bcsc"].includes(provider)) {
    uri += "?ref=";
    uri += provider === "bceidbusiness" ? "external" : "individual";
  }
  return uri;
};

export const getRedirectSignOutMap = (): Record<string, string> => {
  const map = {};
  const providers = ["bceidbusiness", "bcsc", "idir"];

  // adds one signOut url for each provider
  providers.forEach((provider) => {
    const providerRedirectUri = getProviderRedirectUri(provider);
    const returl = [
      `https://${returlHost}/auth/realms/standard/protocol/openid-connect/logout`,
      `?client_id=${cognitoClientId}`,
      `&post_logout_redirect_uri=${providerRedirectUri}`,
    ].join("");

    const signOutUrl = `${signOutHost}/clp-cgi/logoff.cgi?retnow=1&returl=${returl}`;
    map[provider] = signOutUrl;
  });

  return map;
};

export const redirectSignOutMap = getRedirectSignOutMap();

export const awsconfig = {
  Auth: {
    Cognito: {
      userPoolId: `${cognitoPoolId}`,
      userPoolClientId: `${cognitoClientId}`,
      loginWith: {
        oauth: {
          domain:
            nodeEnv === "test"
              ? backendUrl.replace("http://", "").replace(":8080", ":8081")
              : `${cognitoDomain}.auth.${cognitoRegion}.amazoncognito.com`,
          scopes: ["openid", "profile", "email"],
          redirectSignIn: [`${frontendUrl}/dashboard`],
          redirectSignOut: Object.values(redirectSignOutMap),
          responseType: "code",
        },
        username: "true",
      },
    },
  },
};
