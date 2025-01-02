import React from 'react';
import { useKeycloak } from '@react-keycloak/web';

const ProtectedComponent: React.FC = () => {
    const { keycloak, initialized } = useKeycloak();

    // Wait for Keycloak initialization
    if (!initialized) {
        return <p>Initializing Keycloak...</p>;
    }

    // Redirect to login if the user is not authenticated
    if (!keycloak?.authenticated) {
        keycloak?.login();
        return <p>Redirecting to login...</p>;
    }

    return <p>Welcome, {keycloak.tokenParsed?.preferred_username}!</p>;
};

export default ProtectedComponent;