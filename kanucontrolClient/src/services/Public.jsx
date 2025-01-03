// src/services/PublicPage.jsx
import React from "react";
import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,  
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT,
});

const Public = () => {
  return (
    <div>
      <h1>Public Area</h1>
      <button onClick={() => keycloak.logout()}>
        Log out
      </button>
    </div>
  );
};

export default Public;