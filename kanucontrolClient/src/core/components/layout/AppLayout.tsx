import { Box } from "@mui/material";
import Navigation from "@/core/components/layout/Nav";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import BackFooter from "@/core/components/common/BackFooter";
import { useEffect, useRef, useState } from "react";
import { VermietungProvider } from "@/vermietung/context/VermietungProvider";

import { BackNavigationProvider } from "@/core/context/BackNavigationProvider";
import { useBackNavigation } from "@/core/context/BackNavigationContext";

function AppLayoutContent() {
  const navigate = useNavigate();
  const location = useLocation();

  const { handleBack, detailViewOpen } = useBackNavigation();

  const contentRef = useRef<HTMLDivElement>(null);

  const [floatingFooter, setFloatingFooter] = useState(false);
  const [footerHeight, setFooterHeight] = useState(0);

  const isVermietung = location.pathname.startsWith("/vermietung");

  const isStartMenu =
    location.pathname === "/" ||
    location.pathname === "/startmenue" ||
    location.pathname === "/vermietung";

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

  /*
   * Ermitteln, ob der Inhalt höher als der Viewport ist.
   * Bei langen Listen wird der Footer schwebend angezeigt.
   */
  useEffect(() => {
    const checkContentHeight = () => {
      const content = contentRef.current;

      if (!content) {
        return;
      }

      const rect = content.getBoundingClientRect();

      setFloatingFooter(rect.bottom > window.innerHeight);
    };

    checkContentHeight();

    window.addEventListener("resize", checkContentHeight);

    const observer = new ResizeObserver(checkContentHeight);

    if (contentRef.current) {
      observer.observe(contentRef.current);
    }

    return () => {
      window.removeEventListener("resize", checkContentHeight);
      observer.disconnect();
    };
  }, [location.pathname, detailViewOpen]);

  /*
   * Mobile:
   * - Detailansicht: Footer unter dem Inhalt
   * - Sonstige Seiten: Footer schwebend, wenn der Inhalt lang ist
   *
   * Desktop:
   * - Bisheriges Verhalten
   */
  return (
    <VermietungProvider>
      <Box>
        <Navigation />

        <Box
          ref={contentRef}
          sx={{
            pt: { xs: 2, md: 4 },
            px: { xs: 2, md: 4 },
            pb:
              floatingFooter && !detailViewOpen
                ? `calc(${footerHeight}px + 16px + env(safe-area-inset-bottom))`
                : { xs: 2, md: 4 },
          }}
        >
          <Outlet />

          {/* Mobiler Footer bei geöffneten Detail- und Editmasken */}
          {!isStartMenu && detailViewOpen && (
            <Box
              sx={{
                display: { xs: "block", md: "none" },
                mt: 2,
              }}
            >
              <BackFooter
                floating={false}
                onHeightChange={setFooterHeight}
                actions={[
                  ...(!hideBackButton
                    ? [
                        {
                          label: "Zurück",
                          onClick: () => handleBack(() => navigate(-1)),
                        },
                      ]
                    : []),
                  {
                    label: isVermietung ? "Zurück zur Vermietung" : "Zurück zum Startmenü",
                    path: isVermietung ? "/vermietung" : "/startmenue",
                  },
                ]}
              />
            </Box>
          )}
        </Box>

        {/* Footer für lange Listen und sonstige Seiten auf Mobile */}
        {!isStartMenu && !detailViewOpen && (
          <Box
            sx={{
              display: { xs: "block", md: "none" },
            }}
          >
            <BackFooter
              floating={floatingFooter}
              onHeightChange={setFooterHeight}
              actions={[
                ...(!hideBackButton
                  ? [
                      {
                        label: "Zurück",
                        onClick: () => handleBack(() => navigate(-1)),
                      },
                    ]
                  : []),
                {
                  label: isVermietung ? "Zurück zur Vermietung" : "Zurück zum Startmenü",
                  path: isVermietung ? "/vermietung" : "/startmenue",
                },
              ]}
            />
          </Box>
        )}

        {/* Desktop-Footer */}
        {!isStartMenu && (
          <Box sx={{ display: { xs: "none", md: "block" } }}>
            <BackFooter
              floating={floatingFooter}
              onHeightChange={setFooterHeight}
              actions={[
                ...(!hideBackButton
                  ? [
                      {
                        label: "Zurück",
                        onClick: () => handleBack(() => navigate(-1)),
                      },
                    ]
                  : []),
                {
                  label: isVermietung ? "Zurück zur Vermietung" : "Zurück zum Startmenü",
                  path: isVermietung ? "/vermietung" : "/startmenue",
                },
              ]}
            />
          </Box>
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
