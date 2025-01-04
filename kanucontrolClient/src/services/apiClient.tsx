import axios from "axios";
import keycloak from "../keycloak";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8090/api";
console.log(baseURL); // "http://localhost:8090/api"

const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Pseudocode in an Axios interceptor:
apiClient.interceptors.request.use(async (config) => {
  // Attempt refresh
  try {
    const refreshed = await keycloak.updateToken(30);
    if (!refreshed && !keycloak.authenticated) {
      // If we couldn't refresh and Keycloak says we're not authenticated
      // force logout
      keycloak.logout({ redirectUri: "http://localhost:5173" });
    }
  } catch (error) {
    console.error("Error in apiClient: ", error);
    // If the refresh token is invalid or session ended,
    // also do a logout or navigate to some "logged out" page.
    keycloak.logout({ redirectUri: "http://localhost:5173" });
  }

  // attach the token to config
  config.headers.Authorization = `Bearer ${keycloak.token}`;
  return config;
});

export default apiClient;