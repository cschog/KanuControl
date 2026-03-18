import { Box, Typography, Paper, TextField, Button, Stack, Snackbar, Alert } from "@mui/material";
import { useState, useCallback } from "react";
import type { FC } from "react";
import FinanzgruppenTable from "./FinanzgruppenTable";
import apiClient from "@/api/client/apiClient";
import axios from "axios";

interface Props {
  veranstaltungId: number;
}

const KuerzelPage: FC<Props> = ({ veranstaltungId }) => {
  const [newKuerzel, setNewKuerzel] = useState("");
  const [error, setError] = useState<string | null>(null);

  // 🔁 Trigger für Child-Reload
  const [reloadKey, setReloadKey] = useState(0);

  const triggerReload = useCallback(() => {
    setReloadKey((prev) => prev + 1);
  }, []);

  const handleCreate = async () => {
    const trimmed = newKuerzel.trim();
    if (!trimmed) return;

    try {
      await apiClient.post(`/veranstaltungen/${veranstaltungId}/finanzgruppen`, {
        kuerzel: trimmed,
      });

      setNewKuerzel("");
      triggerReload();
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        setError(error.response?.data?.message ?? "Kürzel konnte nicht erstellt werden");
      } else {
        setError("Ein unerwarteter Fehler ist aufgetreten");
      }
    }
  };

  return (
    <>
      <Box>
        <Typography variant="h6" gutterBottom>
          Kürzel-Verwaltung
        </Typography>

        {/* ➕ Neue Gruppe */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Stack direction="row" spacing={2}>
            <TextField
              size="small"
              label="Neues Kürzel"
              value={newKuerzel}
              onChange={(e) => setNewKuerzel(e.target.value)}
            />
            <Button variant="contained" onClick={handleCreate}>
              Hinzufügen
            </Button>
          </Stack>
        </Paper>

        {/* 📋 Gruppen */}
        <FinanzgruppenTable veranstaltungId={veranstaltungId} reloadKey={reloadKey} />

      </Box>

      {/* ⚠ Fehler Snackbar */}
      <Snackbar open={!!error} autoHideDuration={4000} onClose={() => setError(null)}>
        <Alert severity="error">{error}</Alert>
      </Snackbar>
    </>
  );
};

export default KuerzelPage;
