/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_KEYCLOAK_URL: string;
    readonly VITE_KEYCLOAK_REALM: string;
    readonly VITE_KEYCLOAK_CLIENT: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}