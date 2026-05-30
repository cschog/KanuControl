import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Box, Button, Card, CardContent, CircularProgress, Grid, Typography } from "@mui/material";

import { BottomActionBar } from "@/components/layout/BottomActionBar";

import {
  getPostalCodeStatus,
  getPostalCodeCountries,
  importPostalCodes,
  type PostalCodeStatus,
} from "@/api/services/postalCodeAdminApi";

const COUNTRY_LABELS: Record<string, string> = {
  DE: "Deutschland",
  BE: "Belgien",
  NL: "Niederlande",
  AT: "Österreich",
  CH: "Schweiz",
  LU: "Luxemburg",
};

export default function PostalCodeAdminPage() {
  const navigate = useNavigate();

  const [statuses, setStatuses] = useState<Record<string, PostalCodeStatus>>({});

  const [loading, setLoading] = useState(true);

  const [importingCountry, setImportingCountry] = useState<string | null>(null);

  const [countries, setCountries] = useState<string[]>([]);

  const statusLabels = {
    IDLE: "⚪ Bereit",
    RUNNING: "🟡 Import läuft",
    SUCCESS: "🟢 Erfolgreich",
    FAILED: "🔴 Fehler",
  };

async function loadStatus() {
  try {
    setLoading(true);

    const countryCodes = await getPostalCodeCountries();

    setCountries(countryCodes);

    const results = await Promise.all(countryCodes.map((c) => getPostalCodeStatus(c)));

    setStatuses(Object.fromEntries(results.map((r) => [r.countryCode, r])));
  } finally {
    setLoading(false);
  }
}

  useEffect(() => {
    loadStatus();
  }, []);

  async function handleImport(countryCode: string) {
    try {
      setImportingCountry(countryCode);

      await importPostalCodes(countryCode);

      await loadStatus();
    } finally {
      setImportingCountry(null);
    }
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
        {countries.map((countryCode) => {
          const status = statuses[countryCode];

          return (
            <Grid size={{ xs: 12, md: 4 }} key={countryCode}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {COUNTRY_LABELS[countryCode] ?? countryCode}
                  </Typography>

                  <Typography>Datensätze: {status?.count?.toLocaleString("de-DE") ?? 0}</Typography>

                  <Typography>Quelle: {status?.source ?? "-"}</Typography>

                  <Typography>Letzter Import: {status?.lastImport ?? "-"}</Typography>

                  <Typography sx={{ mt: 1 }}>
                    {status ? statusLabels[status.importStatus] : "⚪ Keine Daten"}
                  </Typography>

                  <Box sx={{ mt: 2 }}>
                    <Button
                      variant="contained"
                      disabled={importingCountry === countryCode}
                      onClick={() => handleImport(countryCode)}
                    >
                      {importingCountry === countryCode
                        ? "Import läuft..."
                        : `${countryCode} importieren`}
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
