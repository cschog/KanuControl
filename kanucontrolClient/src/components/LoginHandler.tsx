import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import keycloak from "../keycloak"; // Ensure this points to your Keycloak instance

const LoginHandler: React.FC = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (keycloak.authenticated) {
      navigate("/startmenue"); // Navigate to a protected route after login
    } else {
      keycloak.login(); // Redirect to Keycloak login if not authenticated
    }
  }, [keycloak.authenticated]);

  return null; // Render nothing during navigation
};

export default LoginHandler;