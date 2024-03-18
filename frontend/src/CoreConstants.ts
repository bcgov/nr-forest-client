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
          redirectSignOut: [`${frontendUrl}/logout`],
          responseType: "code",
        },
        username: "true",
      },
    },
  },
};

// constant
export const maxFileSizePerFile = 1000000 * 20; // 20 mb
export const maxTotalFileSize = 1000000 * 20 * 5; // 100 mb
