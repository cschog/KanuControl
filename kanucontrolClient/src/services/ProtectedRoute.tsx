import React, { useContext } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { ReactKeycloakContext } from "@react-keycloak/web";

const ProtectedRoute: React.FC = () => {
  const { keycloak } = useContext(ReactKeycloakContext);

  return keycloak?.authenticated ? <Outlet /> : <Navigate to="/" />;
};

export default ProtectedRoute;