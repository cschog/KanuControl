import { Box, Grid, Card, CardContent, Typography } from "@mui/material";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import Money from "@/components/common/Money";

const PlanungSummary = () => {
  const einnahmen = 2500;
  const ausgaben = 2200;
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
          <Card sx={{ borderLeft: "6px solid #2e7d32" }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={1}>
                <ArrowUpwardIcon color="success" fontSize="small" />
                <Typography variant="subtitle2">Einnahmen (Plan)</Typography>
              </Box>

              <Money value={einnahmen} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* Ausgaben */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: "6px solid #c62828" }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={1}>
                <ArrowDownwardIcon color="error" fontSize="small" />
                <Typography variant="subtitle2">Ausgaben (Plan)</Typography>
              </Box>

              <Money value={ausgaben} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>

        {/* Saldo */}

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderLeft: "6px solid #1976d2" }}>
            <CardContent>
              <Typography variant="subtitle2">Saldo (Plan)</Typography>

              <Money value={saldo} variant="h4" colorize />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default PlanungSummary;
