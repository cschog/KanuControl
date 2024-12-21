import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App' // Update this if your App component is in a different location
import keycloak from "./keycloak";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import './index.css'; // Update the CSS import if necessary

if (process.env.NODE_ENV === "development") {
  console.log("Clearing localStorage and sessionStorage for development");
  localStorage.clear();
  sessionStorage.clear();
}


ReactDOM.createRoot(document.getElementById("root")!).render(
  
    <ReactKeycloakProvider authClient={keycloak}>
      <App />
    </ReactKeycloakProvider>
  
);
