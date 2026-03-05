import { Box, Grid, Card, CardContent, Typography, Divider } from "@mui/material";
import Money from "@/components/common/Money";

const FinanzenDashboard = () => {
  const planKosten = 1850;
  const istKosten = 1810;

  const diff = planKosten - istKosten;

  const kosten = [
    { name: "Unterkunft", betrag: 900 },
    { name: "Verpflegung", betrag: 600 },
  ];

  const einnahmen = [
    { name: "Teilnehmerbeiträge", betrag: 1200 },
    { name: "Zuschüsse", betrag: 800 },
  ];

  return (
    <Box sx={{ mt: 2 }}>
      {/* PLAN vs IST */}

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
          <Card sx={{ borderLeft: 6, borderColor: diff >= 0 ? "success.main" : "error.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Abweichung</Typography>

              <Money value={diff} variant="h3" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* DETAIL */}

      <Grid container spacing={3} sx={{ mt: 2 }}>
        {/* Kosten */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6">Kosten nach Kategorie</Typography>

              <Divider sx={{ my: 2 }} />

              {kosten.map((k) => (
                <Box
                  key={k.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1,
                  }}
                >
                  <Typography>{k.name}</Typography>

                  <Money value={-k.betrag} colorize />
                </Box>
              ))}
            </CardContent>
          </Card>
        </Grid>

        {/* Einnahmen */}

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6">Einnahmen</Typography>

              <Divider sx={{ my: 2 }} />

              {einnahmen.map((e) => (
                <Box
                  key={e.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1,
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
    </Box>
  );
};

export default FinanzenDashboard;
