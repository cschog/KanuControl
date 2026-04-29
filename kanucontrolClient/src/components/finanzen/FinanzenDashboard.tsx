import { Box, Grid, Card, CardContent, Typography, Divider } from "@mui/material";

import Money from "@/components/common/Money";
import { useNavigate } from "react-router-dom";
import { useAppContext } from "@/context/AppContext";

import { useFinanzenDashboard } from "@/hooks/finanzen/useFinanzenDashboard";

import { BottomActionBar } from "@/components/common/BottomActionBar";

const FinanzenDashboard = () => {
  /* =========================================================
     FAKE DATEN
     ========================================================= */

  const navigate = useNavigate();

  const { active } = useAppContext();

  const { data, isLoading } = useFinanzenDashboard(active?.id);

  const planKosten = data?.planKosten ?? 0;
  const istKosten = data?.istKosten ?? 0;

  const planEinnahmen = data?.planEinnahmen ?? 0;

  const istEinnahmen = data?.istEinnahmen ?? 0;

  const planSaldo = data?.planSaldo ?? 0;

  const istSaldo = data?.istSaldo ?? 0;

  const diffKosten = planKosten - istKosten;

  const diffEinnahmen = istEinnahmen - planEinnahmen;

  const diffSaldo = istSaldo - planSaldo;

  const foerderung = data?.foerderung;

  const kosten = data?.kostenNachKategorie ?? [];

  const einnahmen = data?.einnahmenNachKategorie ?? [];

  const istKostenNachKategorie = data?.istKostenNachKategorie ?? [];

  const istEinnahmenNachKategorie = data?.istEinnahmenNachKategorie ?? [];

  const handleExport = () => {
    console.log("export");
  };

  const handleAbrechnung = () => {
    console.log("abrechnung");
  };

  if (isLoading) {
    return <div>Lade Dashboard...</div>;
  }

  /* ========================================================= */

  return (
    <Box sx={{ mt: 2 }}>
      {/* =====================================================
    KOSTEN KPI
    ===================================================== */}

      <Grid container spacing={3}>
        {/* PLAN KOSTEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Plan Kosten</Typography>

              <Money value={-planKosten} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* IST KOSTEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "error.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Ist Kosten</Typography>

              <Money value={-istKosten} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG KOSTEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffKosten >= 0 ? "success.main" : "error.main",
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Abweichung Kosten</Typography>

              <Money value={diffKosten} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    EINNAHMEN KPI
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN EINNAHMEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "success.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Plan Einnahmen</Typography>

              <Money value={planEinnahmen} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* IST EINNAHMEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Ist Einnahmen</Typography>

              <Money value={istEinnahmen} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG EINNAHMEN */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffEinnahmen >= 0 ? "success.main" : "error.main",
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Abweichung Einnahmen</Typography>

              <Money value={diffEinnahmen} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    SALDO KPI
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN SALDO */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Plan Saldo</Typography>

              <Money value={planSaldo} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* IST SALDO */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Ist Saldo</Typography>

              <Money value={istSaldo} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG SALDO */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffSaldo >= 0 ? "success.main" : "error.main",
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">Abweichung Saldo</Typography>

              <Money value={diffSaldo} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
      FOERDERUNG
      ===================================================== */}

      {foerderung && (
        <Grid container spacing={3} sx={{ mt: 0.5 }}>
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent sx={{ py: 2 }}>
                <Typography variant="h6">Förderung</Typography>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={3}>
                  {/* Förderfähige Teilnehmer */}

                  <Grid
                    size={{ xs: 6, md: 3 }}
                    sx={{
                      textAlign: "center",
                    }}
                  >
                    <Typography variant="caption" color="text.secondary">
                      Förderfähige TN
                    </Typography>

                    <Typography variant="h6">{foerderung.foerderfaehigeTeilnehmer}</Typography>
                  </Grid>

                  {/* Fördertage */}

                  <Grid
                    size={{ xs: 6, md: 3 }}
                    sx={{
                      textAlign: "center",
                    }}
                  >
                    <Typography variant="caption" color="text.secondary">
                      Fördertage
                    </Typography>

                    <Typography variant="h6">{foerderung.foerdertage}</Typography>
                  </Grid>

                  {/* Fördersatz */}

                  <Grid
                    size={{ xs: 6, md: 3 }}
                    sx={{
                      textAlign: "center",
                    }}
                  >
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
                      <Money value={foerderung.foerdersatz} variant="body1" />
                    </Box>
                  </Grid>

                  {/* Gesamt */}

                  <Grid
                    size={{ xs: 6, md: 3 }}
                    sx={{
                      textAlign: "center",
                    }}
                  >
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
                      <Money value={foerderung.gesamtfoerderung} variant="body1" />
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* =====================================================
    DETAILBEREICHE
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN KOSTEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Plan Kosten</Typography>

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

        {/* IST KOSTEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Ist Kosten</Typography>

              <Divider sx={{ my: 2 }} />

              {istKostenNachKategorie.map((k) => (
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
      </Grid>

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN EINNAHMEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Plan Einnahmen</Typography>

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

        {/* IST EINNAHMEN */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6">Ist Einnahmen</Typography>

              <Divider sx={{ my: 2 }} />

              {istEinnahmenNachKategorie.map((e) => (
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
