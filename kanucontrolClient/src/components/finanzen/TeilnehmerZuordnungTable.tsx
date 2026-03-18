import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Select,
  MenuItem,
  Snackbar,
  Alert,
} from "@mui/material";
import { useEffect, useState, useCallback } from "react";
import type { FC } from "react";
import axios from "axios";
import apiClient from "@/api/client/apiClient";

interface Props {
  veranstaltungId: number;
}

interface Teilnehmer {
  id: number;
  person: {
    id: number;
    vorname: string;
    name: string;
  };
  kuerzel?: string;
}

interface FinanzGruppe {
  id: number;
  kuerzel: string;
}

const SYSTEM_KUERZEL = "__SYSTEM__";

const TeilnehmerZuordnungTable: FC<Props> = ({ veranstaltungId }) => {
  const [teilnehmer, setTeilnehmer] = useState<Teilnehmer[]>([]);
  const [gruppen, setGruppen] = useState<FinanzGruppe[]>([]);
  const [error, setError] = useState<string | null>(null);

  /* =========================================================
     LOAD
  ========================================================= */

 const load = useCallback(async () => {
   try {
     const [tRes, gRes] = await Promise.all([
       apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer`),
       apiClient.get(`/veranstaltungen/${veranstaltungId}/finanzgruppen`),
     ]);

     setTeilnehmer(tRes.data.content ?? []);
     setGruppen(gRes.data);
   } catch (error: unknown) {
     if (axios.isAxiosError(error)) {
       setError(error.response?.data?.message ?? "Daten konnten nicht geladen werden");
     } else {
       setError("Ein unerwarteter Fehler ist aufgetreten");
     }
   }
 }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load]);

  /* =========================================================
     ASSIGN KUERZEL
  ========================================================= */

  const handleChange = async (teilnehmerId: number, kuerzel: string) => {
    try {
      await apiClient.put(
        `/veranstaltungen/${veranstaltungId}/teilnehmer/${teilnehmerId}/kuerzel`,
        null,
        {
          params: { kuerzel },
        },
      );

      setTeilnehmer((prev) => prev.map((t) => (t.id === teilnehmerId ? { ...t, kuerzel } : t)));
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        setError(error.response?.data?.message ?? "Kürzel konnte nicht zugewiesen werden");
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
      <Paper sx={{ mt: 3 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Teilnehmer</TableCell>
              <TableCell>Kürzel</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {teilnehmer.map((t) => (
              <TableRow key={t.id}>
                <TableCell>
                  {t.person.name}, {t.person.vorname}
                </TableCell>

                <TableCell>
                  {t.kuerzel ? (
                    <Select
                      size="small"
                      value={t.kuerzel}
                      onChange={(e) => handleChange(t.id, e.target.value as string)}
                      sx={{ minWidth: 140 }}
                    >
                      {gruppen
                        .filter((g) => g.kuerzel !== SYSTEM_KUERZEL)
                        .map((g) => (
                          <MenuItem key={g.id} value={g.kuerzel}>
                            {g.kuerzel}
                          </MenuItem>
                        ))}

                      <MenuItem value="">
                        <em>Kürzel entfernen</em>
                      </MenuItem>
                    </Select>
                  ) : (
                    <Select
                      size="small"
                      displayEmpty
                      value=""
                      onChange={(e) => handleChange(t.id, e.target.value as string)}
                      sx={{ minWidth: 140 }}
                    >
                      <MenuItem value="">
                        <em>Kürzel auswählen…</em>
                      </MenuItem>

                      {gruppen
                        .filter((g) => g.kuerzel !== SYSTEM_KUERZEL)
                        .map((g) => (
                          <MenuItem key={g.id} value={g.kuerzel}>
                            {g.kuerzel}
                          </MenuItem>
                        ))}
                    </Select>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <Snackbar open={!!error} autoHideDuration={4000} onClose={() => setError(null)}>
        <Alert severity="error">{error}</Alert>
      </Snackbar>
    </>
  );
};

export default TeilnehmerZuordnungTable;
