// src/pages/admin/AuditPage.tsx

import { Box, Card, CardContent, Grid, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useAuditDashboard } from "@/hooks/audit/useAuditDashboard";

export default function AuditPage() {
  const navigate = useNavigate();

  const { data } = useAuditDashboard();

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Audit & Sicherheit
      </Typography>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 6, md: 3 }}>
          <Card
            onClick={() => navigate("/admin/audit/active-sessions")}
            sx={{
              cursor: "pointer",
              borderLeft: 6,
              borderColor: "primary.main",
            }}
          >
            <CardContent
              sx={{
                py: 2,
                textAlign: "center",
              }}
            >
              <Typography variant="subtitle2" color="text.secondary">
                Aktive Sitzungen
              </Typography>

              <Typography variant="h4" fontWeight={700}>
                {data?.activeSessions ?? 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, md: 3 }}>
          <Card
            onClick={() => navigate("/admin/audit/history")}
            sx={{
              cursor: "pointer",
              borderLeft: 6,
              borderColor: "success.main",
            }}
          >
            <CardContent
              sx={{
                py: 2,
                textAlign: "center",
              }}
            >
              <Typography variant="subtitle2" color="text.secondary">
                Logins heute
              </Typography>

              <Typography variant="h4" fontWeight={700}>
                {data?.loginsToday ?? 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, md: 3 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: "warning.main",
            }}
          >
            <CardContent
              sx={{
                py: 2,
                textAlign: "center",
              }}
            >
              <Typography variant="subtitle2" color="text.secondary">
                Externe Benutzer
              </Typography>

              <Typography
                variant="h4"
                fontWeight={700}
                color={(data?.externalSessions ?? 0) > 0 ? "warning.main" : undefined}
              >
                {data?.externalSessions ?? 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, md: 3 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: "secondary.main",
            }}
          >
            <CardContent
              sx={{
                py: 2,
                textAlign: "center",
              }}
            >
              <Typography variant="subtitle2" color="text.secondary">
                Aktive Vereine
              </Typography>

              <Typography variant="h4" fontWeight={700}>
                {data?.activeTenants ?? 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
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
