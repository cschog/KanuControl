import keycloak from "@/core/auth/keycloak";

export type TenantInfo = {
  tenantId?: string;
  displayName?: string;
};

/**
 * Minimale, sichere Token-Basis
 * (wir nutzen nur Felder, die wir wirklich brauchen)
 */
type BaseToken = {
  sub?: string;
  preferred_username?: string;
  groups?: string[];
  tenant?: string;
  realm_access?: {
    roles?: string[];
  };
};

export function useTenant(): TenantInfo | null {
  if (!keycloak.authenticated || !keycloak.tokenParsed) {
    return null;
  }

  const token = keycloak.tokenParsed as BaseToken;

  // 🔹 Variante A: Custom Claim (empfohlen)
  if (token.tenant) {
    return {
      tenantId: token.tenant,
      displayName: token.tenant,
    };
  }

  // 🔹 Variante B: Groups
  if (Array.isArray(token.groups) && token.groups.length > 0) {
    return {
      tenantId: token.groups[0],
      displayName: token.groups[0],
    };
  }

  return null;
}
export function getUsername(): string | undefined {
  if (!keycloak.authenticated || !keycloak.tokenParsed) {
    return undefined;
  }

  const token = keycloak.tokenParsed as BaseToken;

  return token.preferred_username;
}

export function hasRole(role: string): boolean {
  if (!keycloak.authenticated || !keycloak.tokenParsed) {
    return false;
  }

  const token = keycloak.tokenParsed as BaseToken;

  return token.realm_access?.roles?.includes(role) ?? false;
}

export function isAdmin(): boolean {
  return hasRole("ADMIN");
}
