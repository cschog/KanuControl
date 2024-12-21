declare module "keycloak-js";

declare module '@react-keycloak/web' {
    import React from 'react';

    interface ReactKeycloakProviderProps {
        authClient: any;
        initOptions?: any;
        onEvent?: any;
        onTokens?: any;
        children: React.ReactNode;
    }

    export class ReactKeycloakProvider extends React.Component<ReactKeycloakProviderProps> {}
}