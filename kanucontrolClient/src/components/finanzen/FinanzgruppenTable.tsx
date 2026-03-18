import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  IconButton,
  TextField,
  Snackbar,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from "@mui/material";
import { Edit, Delete, Save, Close } from "@mui/icons-material";
import { useEffect, useState, useCallback } from "react";
import type { FC } from "react";
import axios from "axios";
import apiClient from "@/api/client/apiClient";
import TeilnehmerDialog from "@/components/teilnehmer/TeilnehmerDialog";
import { Chip, Stack } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

interface Props {
  veranstaltungId: number;
  reloadKey?: number;
}

interface TeilnehmerKurz {
  id: number;
  vorname: string;
  nachname: string;
}

interface FinanzGruppe {
  id: number;
  kuerzel: string;
  belegCount: number;
  teilnehmerCount: number;
  teilnehmer: TeilnehmerKurz[];
}

const SYSTEM_KUERZEL = "__SYSTEM__";

const FinanzgruppenTable: FC<Props> = ({ veranstaltungId, reloadKey }) => {
  const [gruppen, setGruppen] = useState<FinanzGruppe[]>([]);
  const [editId, setEditId] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  /* =========================================================
     LOAD
  ========================================================= */

  const [dialogGruppeId, setDialogGruppeId] = useState<number | null>(null);

  const load = useCallback(async () => {
    try {
      const res = await apiClient.get<FinanzGruppe[]>(
        `/veranstaltungen/${veranstaltungId}/finanzgruppen`,
      );
      setGruppen(res.data);
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        setError(error.response?.data?.message ?? "Finanzgruppen konnten nicht geladen werden");
      } else {
        setError("Ein unerwarteter Fehler ist aufgetreten");
      }
    }
  }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load, reloadKey]);

  /* =========================================================
     UPDATE
  ========================================================= */

  const handleSave = async (id: number) => {
    try {
      await apiClient.put(`/veranstaltungen/${veranstaltungId}/finanzgruppen/${id}`, {
        kuerzel: editValue,
      });

      setEditId(null);
      await load();
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        setError(error.response?.data?.message ?? "Kürzel konnte nicht geändert werden");
      } else {
        setError("Ein unerwarteter Fehler ist aufgetreten");
      }
    }
  };

  /* =========================================================
     DELETE
  ========================================================= */

  const handleDelete = async () => {
    if (!deleteId) return;

    try {
      await apiClient.delete(`/veranstaltungen/${veranstaltungId}/finanzgruppen/${deleteId}`);

      setDeleteId(null);
      await load();
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        setError(error.response?.data?.message ?? "Gruppe kann nicht gelöscht werden");
      } else {
        setError("Ein unerwarteter Fehler ist aufgetreten");
      }
    }
  };

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <>
      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Kürzel</TableCell>
              <TableCell align="right">Teilnehmer</TableCell>
              <TableCell align="right">Belege</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {gruppen.map((g) => {
              const isSystem = g.kuerzel === SYSTEM_KUERZEL;
              const isEditable = !isSystem && g.belegCount === 0;

              return (
                <>
                  {/* Hauptzeile */}
                  <TableRow key={g.id}>
                    <TableCell>
                      {editId === g.id ? (
                        <TextField
                          size="small"
                          value={editValue}
                          onChange={(e) => setEditValue(e.target.value)}
                        />
                      ) : (
                        g.kuerzel
                      )}
                    </TableCell>

                    <TableCell align="right">{g.teilnehmerCount}</TableCell>
                    <TableCell align="right">{g.belegCount}</TableCell>

                    <TableCell align="right">
                      {editId === g.id ? (
                        <>
                          <IconButton size="small" onClick={() => handleSave(g.id)}>
                            <Save fontSize="small" />
                          </IconButton>
                          <IconButton size="small" onClick={() => setEditId(null)}>
                            <Close fontSize="small" />
                          </IconButton>
                        </>
                      ) : (
                        <>
                          <IconButton
                            size="small"
                            disabled={!isEditable}
                            onClick={() => {
                              setEditId(g.id);
                              setEditValue(g.kuerzel);
                            }}
                          >
                            <Edit fontSize="small" />
                          </IconButton>

                          <IconButton
                            size="small"
                            disabled={!isEditable}
                            onClick={() => setDeleteId(g.id)}
                          >
                            <Delete fontSize="small" />
                          </IconButton>
                        </>
                      )}
                    </TableCell>
                  </TableRow>

                  {/* Teilnehmer-Zeile */}
                  <TableRow>
                    <TableCell colSpan={4}>
                      <Stack direction="row" spacing={1} flexWrap="wrap">
                        {g.teilnehmer?.map((t) => (
                          <Chip
                            key={t.id}
                            label={`${t.nachname}, ${t.vorname}`}
                            size="small"
                            color="primary"
                          />
                        ))}

                        <Chip
                          icon={<AddIcon />}
                          label="Teilnehmer"
                          variant="outlined"
                          clickable
                          onClick={() => setDialogGruppeId(g.id)}
                        />
                      </Stack>
                    </TableCell>
                  </TableRow>
                </>
              );
            })}
          </TableBody>
        </Table>
      </Paper>

      <TeilnehmerDialog
        open={dialogGruppeId !== null}
        veranstaltungId={veranstaltungId}
        gruppeId={dialogGruppeId ?? 0}
        onClose={() => setDialogGruppeId(null)}
        onSaved={load}
      />

      {/* Delete Dialog */}
      <Dialog open={deleteId !== null} onClose={() => setDeleteId(null)}>
        <DialogTitle>Gruppe löschen?</DialogTitle>
        <DialogContent>Diese Aktion kann nicht rückgängig gemacht werden.</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteId(null)}>Abbrechen</Button>
          <Button color="error" onClick={handleDelete}>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      {/* Error Snackbar */}
      <Snackbar open={!!error} autoHideDuration={4000} onClose={() => setError(null)}>
        <Alert severity="error">{error}</Alert>
      </Snackbar>
    </>
  );
};

export default FinanzgruppenTable;
