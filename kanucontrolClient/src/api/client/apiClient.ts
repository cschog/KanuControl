import axios, { InternalAxiosRequestConfig } from "axios";
import keycloak from "@/auth/keycloak";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8090/api";

const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

/* =========================
   REQUEST
========================= */
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {

    if (keycloak.authenticated) {
      await keycloak.updateToken(30);

      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }

    return config;
  }
);

/* =========================
   RESPONSE
========================= */
apiClient.interceptors.response.use(
  response => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        console.warn("401 – logout");
        keycloak.logout({ redirectUri: window.location.origin });
      }
    } else {
      console.warn("Non-Axios error (network / abort / redirect)", error);
    }

    return Promise.reject(error);
  }
);

export default apiClient;