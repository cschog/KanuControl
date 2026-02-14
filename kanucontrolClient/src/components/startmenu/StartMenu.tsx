import { Box, Button, Alert } from "@mui/material";
import Grid from "@mui/material/Grid";
import { useNavigate } from "react-router-dom";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { useAppContext } from "@/context/AppContext";

const StartMenue = () => {
  const { schema, active, loading } = useAppContext();
  const navigate = useNavigate();

  const contextText = active
    ? `Mandant: ${schema} · ${active.name} · ${active.leiter?.vorname ?? ""} ${
        active.leiter?.name ?? ""
      }`
    : `Mandant: ${schema}`;

  const buttons = [
    { label: "Vereine", path: "/vereine" },
    { label: "Mitglieder", path: "/personen" },
    { label: "Veranstaltungen", path: "/veranstaltungen" },
    { label: "Teilnehmer", path: "/teilnehmer" },
    { label: "Kosten", path: "/kosten" },
    { label: "Reisekosten", path: "/reisekosten" },
    { label: "Anmeldung", path: "/anmeldung" },
    { label: "Abrechnung", path: "/abrechnung" },
    { label: "Teilnehmerliste", path: "/teilnehmerliste" },
    { label: "Reisekosten Ausgabe", path: "/ausgabeReisekosten" },
    { label: "Erhebungsbogen", path: "/erhebungsbogen" },
  ] as const;

  return (
    <Box>
      <MenueHeader contextText={contextText} />

      {loading && <Alert severity="info">Lade Kontext…</Alert>}

      <Grid container spacing={2}>
        {buttons.map((btn) => (
          <Grid key={btn.label} size={{ xs: 12, sm: 6, md: 4 }}>
            <Button fullWidth size="large" variant="contained" onClick={() => navigate(btn.path)}>
              {btn.label}
            </Button>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default StartMenue;
