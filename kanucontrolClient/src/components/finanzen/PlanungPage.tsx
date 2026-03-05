import { Box, Typography, Grid, Card, CardContent, Button } from "@mui/material";
import PlanungSummary from "@/components/finanzen/PlanungSummary";
import PlanungTable from "@/components/finanzen/PlanungTable";

const PlanungPage = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Finanzplanung
      </Typography>

      {/* Finanzierung Status */}

      <PlanungSummary />

      <Grid container spacing={3} mt={1}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6">Plan Ausgaben</Typography>

              <PlanungTable type="AUSGABEN" />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6">Plan Einnahmen</Typography>

              <PlanungTable type="EINNAHMEN" />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box mt={3}>
        <Button variant="contained" color="primary">
          Planung einreichen
        </Button>
      </Box>
    </Box>
  );
};

export default PlanungPage;
