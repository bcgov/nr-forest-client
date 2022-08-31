export const backendUrl =
  config.VITE_BACKEND_URL || import.meta.env.VITE_BACKEND_URL;

export const maxFileSizePerFile = 1000000 * 20; //20 mb
export const maxTotalFileSize = 1000000 * 20 * 5; //100 mb
