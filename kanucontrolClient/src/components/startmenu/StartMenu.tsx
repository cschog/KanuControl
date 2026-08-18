import { Box, Alert } from "@mui/material";
import Grid from "@mui/material/Grid";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getAllVereine } from "@/api/services/vereinApi";
import { getPersonsPaged } from "@/api/services/personApi";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { useAppContext } from "@/context/AppContext";
import { ModuleButton } from "@/components/common/ModuleButton";
import { moduleTypeMap } from "@/theme/moduleMap";
import { FeedbackFab } from "@/components/userFeedBack/featureBase/FeedbackFab";
import { isAdmin } from "@/auth/useTenant";

const StartMenue = () => {
  const { schema, active, loading } = useAppContext();
  const navigate = useNavigate();

  const [vereinCount, setVereinCount] = useState(0);
  const [personenCount, setPersonenCount] = useState(0);
  const [loadingStammdaten, setLoadingStammdaten] = useState(true);

  useEffect(() => {
    const loadStammdaten = async () => {
      try {
        setLoadingStammdaten(true);

        const [vereine, personen] = await Promise.all([getAllVereine(), getPersonsPaged(0, 1)]);

        setVereinCount(vereine.length);
        setPersonenCount(personen.totalElements);
      } catch (error) {
        console.error("Fehler beim Laden der Stammdaten", error);
      } finally {
        setLoadingStammdaten(false);
      }
    };

    loadStammdaten();
  }, []);

  const contextText = active
    ? `Mandant: ${schema} · ${active.name} · ${active.leiter?.vorname ?? ""} ${
        active.leiter?.name ?? ""
      }`
    : `Mandant: ${schema}`;

  const admin = isAdmin();

const allgemeineButtons = [
  {
    key: "vereine",
    label: "Vereine",
    path: "/vereine",
    disabled: false,
  },
  {
    key: "mitglieder",
    label: "Mitglieder",
    path: "/personen",
    disabled: vereinCount === 0,
  },
  {
    key: "veranstaltungen",
    label: "Veranstaltungen",
    path: "/veranstaltungen",
    disabled: vereinCount === 0 || personenCount === 0,
  },
  {
    key: "teilnehmer",
    label: "Teilnehmer",
    path: "/teilnehmer",
    disabled: !active,
  },
  {
    key: "dokumente",
    label: "Dokumente",
    path: "/dokumente",
    disabled: !active,
  },
  {
    key: "verwaltung",
    label: "Verwaltung",
    path: "/verwaltung",
    disabled: false,
  },
] as const;

  const finanzBereiche = active?.id
    ? [
        {
          key: "vorbereitung",
          label: "Vorbereitung",
          path: `/veranstaltungen/${active.id}/finanzen/vorbereitung`,
        },
        {
          key: "durchfuehrung",
          label: "Durchführung",
          path: `/veranstaltungen/${active.id}/finanzen/durchfuehrung`,
        },
        {
          key: "auswertung",
          label: "Auswertung",
          path: `/veranstaltungen/${active.id}/finanzen/auswertung`,
        },
      ]
    : [];

  return (
    <Box>
      <MenueHeader contextText={contextText} />

      {loading && <Alert severity="info">Lade Kontext…</Alert>}

      {/* =====================================================
          ALLGEMEINE MODULE
         ===================================================== */}

      <Grid container spacing={2}>
        {allgemeineButtons.map((btn) => (
          <Grid key={btn.key} size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleButton
              label={btn.label}
              moduleType={moduleTypeMap[btn.key]}
              disabled={btn.disabled || loadingStammdaten}
              onClick={() => navigate(btn.path)}
            />
          </Grid>
        ))}
      </Grid>

      {/* =====================================================
          FINANZBEREICHE
         ===================================================== */}

      {finanzBereiche.length > 0 && (
        <Grid container spacing={2} sx={{ mt: 2 }}>
          {finanzBereiche.map((bereich) => (
            <Grid key={bereich.key} size={{ xs: 12, sm: 6, md: 4 }}>
              <ModuleButton
                label={bereich.label}
                moduleType={moduleTypeMap.finanzen}
                onClick={() => navigate(bereich.path)}
              />
            </Grid>
          ))}
        </Grid>
      )}

      {/* =====================================================
          ADMINISTRATION – IMMER GANZ UNTEN
         ===================================================== */}

      {admin && (
        <Grid container spacing={2} sx={{ mt: 2 }}>
          <Grid size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleButton
              label="Administration"
              moduleType={moduleTypeMap.admin}
              onClick={() => navigate("/admin")}
            />
          </Grid>
        </Grid>
      )}

      <FeedbackFab />
    </Box>
  );
};

export default StartMenue;
