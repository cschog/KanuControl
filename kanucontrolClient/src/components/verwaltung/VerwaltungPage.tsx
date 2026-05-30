import { Box, Tab, Tabs } from "@mui/material";
import { useState, Suspense, lazy } from "react";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";

/* =========================================================
   LAZY IMPORTS
   ========================================================= */

const BeitragsStrukturen = lazy(
  () => import("@/components/verwaltung/beitraege/BeitragsstrukturTable"),
);

/* =========================================================
   COMPONENT
   ========================================================= */

const VerwaltungPage = () => {
  const [tab, setTab] = useState(() => Number(localStorage.getItem("verwaltungTab")) || 0);

  const navigate = useNavigate();

  /* =========================================================
     TAB CHANGE
     ========================================================= */

  const handleChange = (_event: React.SyntheticEvent, value: number) => {
    setTab(value);

    localStorage.setItem("verwaltungTab", String(value));
  };

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box>
      {/* =====================================================
          TABS
          ===================================================== */}

      <Tabs
        value={tab}
        onChange={handleChange}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        sx={{
          mb: 2,

          minHeight: {
            xs: 42,
            md: 48,
          },

          "& .MuiTab-root": {
            minHeight: {
              xs: 42,
              md: 48,
            },

            fontSize: {
              xs: "0.8rem",
              md: "0.95rem",
            },

            px: {
              xs: 1.5,
              md: 2,
            },

            textTransform: "none",

            fontWeight: 600,
          },
        }}
      >
        <Tab label="Beitragsstrukturen" />
      </Tabs>

      {/* =====================================================
          CONTENT
          ===================================================== */}

      <Suspense fallback={<div>Loading...</div>}>
        <Box
          sx={{
            overflowX: "hidden",
          }}
        >
          {tab === 0 && <BeitragsStrukturen />}
        </Box>
      </Suspense>

      {/* =====================================================
          ACTION BAR
          ===================================================== */}

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/startmenue"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
};

export default VerwaltungPage;
