// src/pages/admin/ActiveSessionsPage.tsx

import {
  Box,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useActiveSessions } from "@/hooks/audit/useActiveSessions";

export default function ActiveSessionsPage() {
  const navigate = useNavigate();

  const { data, isLoading } = useActiveSessions();

  if (isLoading) {
    return <CircularProgress />;
  }

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Aktive Sitzungen: {data?.length ?? 0}
      </Typography>

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Benutzer</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Verein</TableCell>
              <TableCell>IP</TableCell>
              <TableCell>Login</TableCell>
              <TableCell>Letzte Aktivität</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {data?.map((row) => {
            
              return (
                <TableRow key={row.id}>
                  <TableCell>{row.username}</TableCell>
                  <TableCell>{row.fullName}</TableCell>
                  <TableCell>{row.tenant}</TableCell>
                  <TableCell>
                    {row.ipAddress?.startsWith("192.168.100.") ? (
                      "Intern"
                    ) : (
                      <Typography component="span" color="warning.main" fontWeight={700}>
                        Extern
                      </Typography>
                    )}
                  </TableCell>

                  <TableCell>{new Date(row.loginTime).toLocaleString("de-DE")}</TableCell>

                  <TableCell>{new Date(row.lastSeen).toLocaleString("de-DE")}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Paper>

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
