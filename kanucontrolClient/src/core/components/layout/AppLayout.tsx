import { Box, CircularProgress } from "@mui/material";
import Navigation from "@/core/components/layout/Nav";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import BackFooter from "@/core/components/common/BackFooter";
import { Suspense, useEffect, useRef } from "react";
import { VermietungProvider } from "@/vermietung/context/VermietungProvider";

import { BackNavigationProvider } from "@/core/context/BackNavigationProvider";
import { useBackNavigation } from "@/core/context/BackNavigationContext";

function AppLayoutContent() {
  const navigate = useNavigate();
  const location = useLocation();

  const { handleBack } = useBackNavigation();

  const contentRef = useRef<HTMLDivElement>(null);


  const isFahrten = location.pathname.startsWith("/fahrten");
  const modulStart = isFahrten ? "/fahrten" : location.pathname.startsWith("/vermietung") ? "/vermietung" : "/startmenue";
  const modulZurueck = isFahrten ? "Zurück zu Fahrten" : location.pathname.startsWith("/vermietung") ? "Zurück zur Vermietung" : "Zurück zum Startmenü";

  const isStartMenu =
    location.pathname === "/" ||
    location.pathname === "/startmenue" ||
    location.pathname === "/vermietung" ||
    location.pathname === "/fahrten";

  const hideBackButtonOn = [
    "/veranstaltungen",
    "/verwaltung",
    "/dokumente",
    "/vereine",
    "/admin",
    "/personen",
    "/teilnehmer",
  ];

  const hideBackButton =
    hideBackButtonOn.includes(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/vorbereitung$/.test(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/durchfuehrung$/.test(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/auswertung$/.test(location.pathname);

  useEffect(() => {
    contentRef.current?.scrollTo({ top: 0, left: 0 });
  }, [location.pathname]);

  return (
    <VermietungProvider>
      <Box
        sx={{
          display: "grid",
          gridTemplateRows: "auto minmax(0, 1fr) auto",
          height: "100vh",
          "@supports (height: 100dvh)": { height: "100dvh" },
          overflow: "hidden",
        }}
      >
        <Navigation />

        <Box
          component="main"
          ref={contentRef}
          sx={{
            minHeight: 0,
            minWidth: 0,
            overflow: "auto",
            pt: { xs: 2, md: 4 },
            px: { xs: 2, md: 4 },
            pb: { xs: 2, md: 4 },
          }}
        >
          <Suspense fallback={
            <Box role="status" sx={{ display: "flex", justifyContent: "center", py: 4 }}>
              <CircularProgress aria-label="Seite wird geladen" />
            </Box>
          }>
            <Outlet />
          </Suspense>
        </Box>

        {!isStartMenu && (
          <BackFooter
            actions={[
              ...(!hideBackButton
                ? [{ label: "Zurück", onClick: () => handleBack(() => navigate(-1)) }]
                : []),
              { label: modulZurueck, path: modulStart },
            ]}
          />
        )}
      </Box>
    </VermietungProvider>
  );
}

export default function AppLayout() {
  return (
    <BackNavigationProvider>
      <AppLayoutContent />
    </BackNavigationProvider>
  );
}
