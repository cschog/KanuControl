import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:8080",
    realm: "KanuControl",
    clientId: "kc_client",
  });

keycloak.init({
  onLoad: "login-required", // or 'check-sso'
  checkLoginIframe: false, // Skip iframe check
  redirectUri: "http://localhost:3000/callback", // Ensure this matches the configured URI
});

export default keycloak;