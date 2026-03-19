import axios, { InternalAxiosRequestConfig } from "axios";
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
    // Wenn noch nicht authenticated → warten
    if (!keycloak.authenticated) {
      await keycloak.init({
        onLoad: "login-required",
        checkLoginIframe: false,
      });
    }

    // Token aktualisieren
    await keycloak.updateToken(30);

    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${keycloak.token}`;

    return config;
  },
  (error) => Promise.reject(error),
);

/* =========================
   RESPONSE INTERCEPTOR
========================= */


apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      try {
        await keycloak.updateToken(0);
        return apiClient(error.config); // retry request
      } catch {
        keycloak.login();
      }
    }

    return Promise.reject(error);
  },
);

export default apiClient;
