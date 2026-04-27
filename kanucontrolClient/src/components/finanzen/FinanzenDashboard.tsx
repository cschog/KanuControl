import { Box, Grid, Card, CardContent, Typography, Divider } from "@mui/material";

import Money from "@/components/common/Money";
import { useNavigate } from "react-router-dom";

import {BottomActionBar} from "@/components/common/BottomActionBar";

const FinanzenDashboard = () => {
  /* =========================================================
     FAKE DATEN
     ========================================================= */

  const planKosten = 1850;
  const istKosten = 1810;

  const diff = planKosten - istKosten;

  const navigate = useNavigate();

  const handleExport = () => {
    console.log("export");
  };

  const handleAbrechnung = () => {
    console.log("abrechnung");
  };

  const kosten = [
    { name: "Unterkunft", betrag: 900 },
    { name: "Verpflegung", betrag: 600 },
    { name: "Fahrtkosten", betrag: 310 },
  ];

  const einnahmen = [
    { name: "Teilnehmerbeiträge", betrag: 1200 },
    { name: "Zuschüsse", betrag: 800 },
  ];

  const foerderung = {
    foerderfaehigeTeilnehmer: 12,
    foerdertage: 9,
    foerdersatz: 14,
    kikZuschlag: 3,
    deckel: 15,
    gesamtfoerderung: 1620,
  };

  /* ========================================================= */

  return (
    <Box sx={{ mt: 2 }}>
      {/* =====================================================
          KPI CARDS
          ===================================================== */}

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Plan Kosten</Typography>

              <Money value={-planKosten} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "error.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Ist Kosten</Typography>

              <Money value={-istKosten} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diff >= 0 ? "success.main" : "error.main",
            }}
          >
            <CardContent>
              <Typography variant="subtitle2">Abweichung</Typography>

              <Money value={diff} variant="h3" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    FOERDERUNG
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6">Förderung</Typography>

              <Divider sx={{ my: 2 }} />

              <Grid container spacing={3}>
                {/* Förderfähige Teilnehmer */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    Förderfähige TN
                  </Typography>

                  <Typography variant="h5" sx={{ mt: 0.5 }}>
                    {foerderung.foerderfaehigeTeilnehmer}
                  </Typography>
                </Grid>

                {/* Fördertage */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    Fördertage
                  </Typography>

                  <Typography variant="h5" sx={{ mt: 0.5 }}>
                    {foerderung.foerdertage}
                  </Typography>
                </Grid>

                {/* Fördersatz */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    Fördersatz
                  </Typography>

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "center",
                      mt: 0.5,
                    }}
                  >
                    <Money value={foerderung.foerdersatz} variant="h5" />
                  </Box>
                </Grid>

                {/* KiK-Zuschlag */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    KiK-Zuschlag
                  </Typography>

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "center",
                      mt: 0.5,
                    }}
                  >
                    <Money value={foerderung.kikZuschlag} variant="h5" />
                  </Box>
                </Grid>

                {/* Deckel */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    Deckel
                  </Typography>

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "center",
                      mt: 0.5,
                    }}
                  >
                    <Money value={foerderung.deckel} variant="h5" />
                  </Box>
                </Grid>

                {/* Gesamtförderung */}

                <Grid size={{ xs: 6, md: 2 }} sx={{ textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary">
                    Gesamtförderung
                  </Typography>

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "center",
                      mt: 0.5,
                    }}
                  >
                    <Money value={foerderung.gesamtfoerderung} variant="h5" colorize />
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
          DETAILBEREICHE
          ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* KOSTEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Kosten nach Kategorie</Typography>

              <Divider sx={{ my: 2 }} />

              {kosten.map((k) => (
                <Box
                  key={k.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{k.name}</Typography>

                  <Money value={-k.betrag} colorize />
                </Box>
              ))}
            </CardContent>
          </Card>
        </Grid>

        {/* EINNAHMEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Einnahmen</Typography>

              <Divider sx={{ my: 2 }} />

              {einnahmen.map((e) => (
                <Box
                  key={e.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{e.name}</Typography>

                  <Money value={e.betrag} colorize />
                </Box>
              ))}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate(-1),
            variant: "outlined",
          },
        ]}
        right={[
          {
            label: "Export",
            onClick: handleExport,
            variant: "outlined",
          },
          {
            label: "Abrechnung",
            onClick: handleAbrechnung,
          },
        ]}
      />
    </Box>
  );
};

export default FinanzenDashboard;
