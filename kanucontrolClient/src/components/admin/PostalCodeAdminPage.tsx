import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  Typography,
  Switch,
  FormControlLabel,
} from "@mui/material";

import { BottomActionBar } from "@/components/layout/BottomActionBar";

import {
  getPostalCodeStatus,
  getPostalCodeCountries,
  importPostalCodes,
  updatePostalCodeCountry,
  type PostalCodeStatus,
  type PostalCodeCountry,
} from "@/api/services/postalCodeAdminApi";

const COUNTRY_LABELS: Record<string, string> = {
  DE: "Deutschland",
  BE: "Belgien",
  NL: "Niederlande",
  AT: "Österreich",
  CH: "Schweiz",
  LU: "Luxemburg",
  DK: "Dänemark",
  SI: "Slowenien",
  SE: "Schweden",
  FR: "Frankreich",
  GB: "Großbritannien",
  PL: "Polen",
  CZ: "Tschechien",
  IT: "Italien",
  NO: "Norwegen",
  LI: "Lichtenstein",
  ES: "Spanien",
};

export default function PostalCodeAdminPage() {
  const navigate = useNavigate();

  const [statuses, setStatuses] = useState<Record<string, PostalCodeStatus>>({});

  const [loading, setLoading] = useState(true);

 const [countries, setCountries] = useState<PostalCodeCountry[]>([]);

  const statusLabels = {
    IDLE: "⚪ Bereit",
    RUNNING: "🟡 Import läuft",
    SUCCESS: "🟢 Erfolgreich",
    FAILED: "🔴 Fehler",
  };

async function loadStatus(showSpinner = false) {
  try {
    if (showSpinner) {
      setLoading(true);
    }

    const countries = await getPostalCodeCountries();

    setCountries(countries);

    const results = await Promise.all(countries.map((c) => getPostalCodeStatus(c.countryCode)));

    setStatuses(Object.fromEntries(results.map((r) => [r.countryCode, r])));
  } finally {
    if (showSpinner) {
      setLoading(false);
    }
  }
}

useEffect(() => {
  loadStatus(true);
}, []);

  useEffect(() => {
    const hasRunningImport = Object.values(statuses).some((s) => s.importStatus === "RUNNING");

    if (!hasRunningImport) {
      return;
    }

    const timer = setInterval(() => {
      loadStatus();
    }, 5000);

    return () => clearInterval(timer);
  }, [statuses]);

  function formatDate(value?: string | null) {
    if (!value) return "-";

    return new Date(value).toLocaleDateString("de-DE");
  }

async function handleImport(countryCode: string) {
  await importPostalCodes(countryCode);

  await loadStatus();
}

  if (loading) {
    return <CircularProgress />;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        PLZ-Verwaltung
      </Typography>

      <Grid container spacing={2}>
        {countries.map((country) => {
          const status = statuses[country.countryCode];

          return (
            <Grid size={{ xs: 12, md: 4 }} key={country.countryCode}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {COUNTRY_LABELS[country.countryCode] ?? country.countryCode}
                  </Typography>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={country.enabled}
                        onChange={async (_, checked) => {
                          await updatePostalCodeCountry(
                            country.countryCode,
                            checked,
                            country.autoImport,
                          );

                          await loadStatus();
                        }}
                      />
                    }
                    label="Aktiviert"
                  />
                  <FormControlLabel
                    control={
                      <Switch
                        checked={country.autoImport}
                        disabled={!country.enabled}
                        onChange={async (_, checked) => {
                          await updatePostalCodeCountry(
                            country.countryCode,
                            country.enabled,
                            checked,
                          );

                          await loadStatus();
                        }}
                      />
                    }
                    label="Automatischer Import"
                  />
                  <Typography>Datensätze: {status?.count?.toLocaleString("de-DE") ?? 0}</Typography>

                  <Typography>Quelle: {status?.source ?? "-"}</Typography>

                  <Typography>Letzter Import: {formatDate(country.lastImport)}</Typography>

                  <Typography>Nächster Import: {formatDate(country.nextImport)}</Typography>

                  <Typography sx={{ mt: 1 }}>
                    {status ? statusLabels[status.importStatus] : "⚪ Keine Daten"}
                  </Typography>

                  <Box sx={{ mt: 2 }}>
                    <Button
                      variant="contained"
                      disabled={status?.importStatus === "RUNNING"}
                      onClick={() => handleImport(country.countryCode)}
                    >
                      {status?.importStatus === "RUNNING"
                        ? "Import läuft..."
                        : `${country.countryCode} importieren`}
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/admin"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
}
