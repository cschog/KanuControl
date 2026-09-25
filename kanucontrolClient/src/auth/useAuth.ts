import { useEffect, useRef, useState } from "react";
import keycloak from "@/auth/keycloak";
import apiClient from "@/api/client/apiClient";

const INACTIVITY_TIMEOUT = 30 * 60 * 1000; //  30 Minuten
const ACTIVITY_CHECK_INTERVAL = 10 * 1000; // alle 10 Sekunden

let keycloakInitPromise: Promise<boolean> | null = null;

function initKeycloak(): Promise<boolean> {
  if (!keycloakInitPromise) {
    keycloakInitPromise = keycloak.init({
      onLoad: "login-required",
      checkLoginIframe: false,
    });
  }

  return keycloakInitPromise;
}

export function useAuth() {
  const [initialized, setInitialized] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  const lastActivityRef = useRef<number>(Date.now());
  const logoutStartedRef = useRef(false);

  useEffect(() => {

    const handleActivity = () => {
      if (!logoutStartedRef.current) {
        lastActivityRef.current = Date.now();
      }
    };

    const activityEvents = ["mousedown", "keydown", "touchstart", "scroll", "pointerdown"] as const;

    activityEvents.forEach((event) => {
      window.addEventListener(event, handleActivity, { passive: true });
    });

    const handleLogout = async () => {
      if (logoutStartedRef.current) {
        return;
      }

      logoutStartedRef.current = true;

      

      // Keycloak-Logout NICHT vom Audit-Request abhängig machen.
      try {
        await apiClient.post("/admin/audit/logout");
      
      } catch (error: unknown) {
        console.error("AUTH: Audit-Logout konnte nicht gespeichert werden", error);
      }

      try {
        await keycloak.logout({
          redirectUri: window.location.origin,
        });
      } catch (error: unknown) {
        console.error("AUTH: Keycloak Logout fehlgeschlagen", error);

        // Falls Keycloak den Redirect nicht ausführt:
        window.location.href = `${keycloak.createLogoutUrl({
          redirectUri: window.location.origin,
        })}`;
      }
    };

    const inactivityInterval = window.setInterval(() => {
      if (!keycloak.authenticated || logoutStartedRef.current) {
        return;
      }

      const inactiveFor = Date.now() - lastActivityRef.current;

      if (inactiveFor >= INACTIVITY_TIMEOUT) {
        void handleLogout();
      }
    }, ACTIVITY_CHECK_INTERVAL);

    initKeycloak()
      .then((auth: boolean) => {

        setAuthenticated(auth);

        if (auth) {
          lastActivityRef.current = Date.now();
        }
      })
      .catch((err: unknown) => {
        console.error("AUTH: Keycloak init failed", err);
        setAuthenticated(false);
      })
      .finally(() => {
        setInitialized(true);
      });

    return () => {

      window.clearInterval(inactivityInterval);

      activityEvents.forEach((event) => {
        window.removeEventListener(event, handleActivity);
      });
    };
  }, []);

  return { initialized, authenticated };
}
