import { Box, Grid, Card, CardContent, Typography } from "@mui/material";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import Money from "@/components/common/Money";

const KostenSummary = () => {
  const einnahmen = 1980;
  const ausgaben = 1810;
  const saldo = einnahmen - ausgaben;

  return (
    <Box
      sx={{
        position: "sticky",
        top: 0,
        zIndex: 10,
        backgroundColor: "background.default",
        pb: 2,
      }}
    >
      <Grid container spacing={3} mb={2}>
        {/* Einnahmen */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "success.main" }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={1}>
                <ArrowUpwardIcon color="success" fontSize="small" />
                <Typography variant="subtitle2">Einnahmen</Typography>
              </Box>

              <Money value={einnahmen} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* Ausgaben */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "error.main" }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={1}>
                <ArrowDownwardIcon color="error" fontSize="small" />
                <Typography variant="subtitle2">Ausgaben</Typography>
              </Box>

              <Money value={-ausgaben} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* Saldo */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Saldo</Typography>

              <Money value={saldo} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default KostenSummary;
