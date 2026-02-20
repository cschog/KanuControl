import { Box, Alert } from "@mui/material";
import Grid from "@mui/material/Grid";
import { useNavigate } from "react-router-dom";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { useAppContext } from "@/context/AppContext";
import { ModuleButton } from "@/components/common/ModuleButton";
import { moduleTypeMap } from "@/theme/moduleMap";

const StartMenue = () => {
  const { schema, active, loading } = useAppContext();
  const navigate = useNavigate();

  const contextText = active
    ? `Mandant: ${schema} · ${active.name} · ${active.leiter?.vorname ?? ""} ${
        active.leiter?.name ?? ""
      }`
    : `Mandant: ${schema}`;

 const buttons = [
   { key: "vereine", label: "Vereine", path: "/vereine" },
   { key: "mitglieder", label: "Mitglieder", path: "/personen" },
   { key: "veranstaltungen", label: "Veranstaltungen", path: "/veranstaltungen" },
   { key: "teilnehmer", label: "Teilnehmer", path: "/teilnehmer" },

   { key: "finanzen", label: "Finanzen", path: "/kosten" },
   { key: "reisekosten", label: "Reisekosten", path: "/reisekosten" },

   { key: "anmeldung", label: "Anmeldung", path: "/anmeldung" },
   { key: "abrechnung", label: "Abrechnung", path: "/abrechnung" },
   { key: "teilnehmerliste", label: "Teilnehmerliste", path: "/teilnehmerliste" },
   { key: "reisekostenausgabe", label: "Reisekosten Ausgabe", path: "/ausgabeReisekosten" },
   { key: "erhebungsbogen", label: "Erhebungsbogen", path: "/erhebungsbogen" },
 ] as const;

  return (
    <Box>
      <MenueHeader contextText={contextText} />

      {loading && <Alert severity="info">Lade Kontext…</Alert>}

      <Grid container spacing={2}>
        {buttons.map((btn) => (
          <Grid key={btn.key} size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleButton
              label={btn.label}
              moduleType={moduleTypeMap[btn.key]}
              onClick={() => navigate(btn.path)}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default StartMenue;
