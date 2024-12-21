import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8090/api";
console.log(baseURL); // "http://localhost:8090/api"

const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

export default apiClient;