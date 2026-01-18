import { useEffect, useState } from "react";
import { Box, Button, Typography, Alert } from "@mui/material";
import Grid from "@mui/material/Grid";
import { useNavigate } from "react-router-dom";
import apiClient from "@/api/client/apiClient";

const StartMenue = () => {
  const [activeSchema, setActiveSchema] = useState("");
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchSchema = async () => {
      try {
        const response = await apiClient.get("/active-schema");
        setActiveSchema(response.data);
      } catch {
        setError("Mandant konnte nicht geladen werden");
      }
    };
    fetchSchema();
  }, []);

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
      <Typography variant="h4" gutterBottom>
        KanuControl
      </Typography>

      <Typography variant="subtitle1" sx={{ mb: 2 }}>
        Mandant:{" "}
        <Typography component="span" fontWeight="bold">
          {activeSchema || "Lädt…"}
        </Typography>
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={2}>
        {buttons.map((btn) => (
          <Grid key={btn.label} size={{ xs: 12, sm: 6, md: 4 }}>
            <Button
              fullWidth
              size="large"
              variant="contained"
              onClick={() => navigate(btn.path)}
            >
              {btn.label}
            </Button>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default StartMenue;