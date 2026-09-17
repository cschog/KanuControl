import { Box } from "@mui/material";
import Navigation from "@/components/layout/Nav";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import BackFooter from "@/components/common/BackFooter";
import { useEffect, useRef, useState } from "react";

export default function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const contentRef = useRef<HTMLDivElement>(null);
  const [floatingFooter, setFloatingFooter] = useState(false);

  const isStartMenu = location.pathname === "/" || location.pathname === "/startmenue";
  const hideBackButtonOn =
    ["/veranstaltungen",
      "/verwaltung",
      "/dokumente",
      "/vereine",
      "/admin",
      "/personen",
      "/teilnehmer"];

  const hideBackButton =
    hideBackButtonOn.includes(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/vorbereitung$/.test(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/durchfuehrung$/.test(location.pathname) ||
    /^\/veranstaltungen\/\d+\/finanzen\/auswertung$/.test(location.pathname);
  

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
  }, []);

  return (
    <Box>
      <Navigation />

      <Box
        ref={contentRef}
        sx={{
          p: { xs: 2, md: 4 },
        }}
      >
        <Outlet />
      </Box>

      {!isStartMenu && (
        <BackFooter
          floating={floatingFooter}
          actions={[
            ...(!hideBackButton
              ? [
                  {
                    label: "Zurück",
                    onClick: () => navigate(-1),
                  },
                ]
              : []),
            {
              label: "Zurück zum Startmenü",
              path: "/startmenue",
            },
          ]}
        />
      )}
    </Box>
  );
}
