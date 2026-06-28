import { Box, Tab, Tabs } from "@mui/material";
import { useState, Suspense, lazy } from "react";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";

/* =========================================================
   LAZY IMPORTS
   ========================================================= */

const BeitragsStrukturen = lazy(
  () => import("@/components/verwaltung/beitraege/BeitragsstrukturPage"),
);

const Unterkunftsarten = lazy(
  () => import("@/components/verwaltung/unterkunft/UnterkunftsartPage"),
);

const Verpflegungsmodelle = lazy(
  () => import("@/components/verwaltung/verpflegung/VerpflegungsmodellPage"),
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
        <Tab label="Unterkunftsarten" />
        <Tab label="Verpflegungsmodelle" />
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
          {tab === 1 && <Unterkunftsarten />}
          {tab === 2 && <Verpflegungsmodelle />}
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
