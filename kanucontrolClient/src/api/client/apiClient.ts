import axios, { InternalAxiosRequestConfig, AxiosResponse } from "axios";
import keycloak from "@/auth/keycloak";

const baseURL = "/api";

const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

/* =========================
   REQUEST INTERCEPTOR
========================= */
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // 🔐 Keycloak Token
    if (keycloak.authenticated) {
      await keycloak.updateToken(30);

      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }

    // 🔍 REQUEST LOG
    // console.groupCollapsed("➡️ API REQUEST");
    // console.log("URL:", `${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    // console.log("PARAMS:", config.params);
    // console.log("DATA:", config.data);
    // console.log("HEADERS:", config.headers);
    // console.groupEnd();

    return config;
  },
  (error) => Promise.reject(error),
);

/* =========================
   RESPONSE INTERCEPTOR
========================= */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 🔍 RESPONSE LOG
    // console.groupCollapsed("⬅️ API RESPONSE");
    // console.log("URL:", response.config.url);
    // console.log("STATUS:", response.status);
    // console.log("DATA:", response.data);
    // console.groupEnd();

    return response;
  },
  async (error: unknown) => {
    if (axios.isAxiosError(error)) {
      // console.groupCollapsed("❌ API ERROR");
      // console.log("URL:", error.config?.url);
      // console.log("STATUS:", error.response?.status);
      // console.log("DATA:", error.response?.data);
      // console.groupEnd();

      // 🔐 401 → Logout
      if (error.response?.status === 401) {
        console.warn("401 – Session expired, logging out");
        await keycloak.logout({
          redirectUri: window.location.origin,
        });
      }
    } else {
      console.error("❌ Non-Axios error", error);
    }

    return Promise.reject(error);
  },
);

export default apiClient;
