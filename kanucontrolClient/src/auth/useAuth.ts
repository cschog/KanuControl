import { useEffect, useState } from "react";
import keycloak from "@/auth/keycloak";

let initializedOnce = false;

export function useAuth() {
  const [initialized, setInitialized] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    if (initializedOnce) return;

    initializedOnce = true;

    keycloak
      .init({
        onLoad: "login-required",
        checkLoginIframe: false,
      })
      .then((auth: boolean) => {
        setAuthenticated(auth);
      })
      .catch((err: unknown) => {
        console.error("Keycloak init failed", err);

        setAuthenticated(false);
      })
      .finally(() => {
        setInitialized(true);
      });
  }, []);

  return { initialized, authenticated };
}
