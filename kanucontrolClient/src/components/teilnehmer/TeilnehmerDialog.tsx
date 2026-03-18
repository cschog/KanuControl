import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Autocomplete,
  TextField,
  CircularProgress,
} from "@mui/material";
import { useEffect, useState } from "react";
import type { FC } from "react";
import apiClient from "@/api/client/apiClient";
import axios from "axios";

/* =========================================================
   Backend Response Typen
========================================================= */

interface TeilnehmerDTO {
  id: number;
  kuerzel?: string;
  person: {
    id: number;
    vorname: string;
    name: string;
  };
}

interface PageResponse<T> {
  content: T[];
}

/* =========================================================
   Dialog Option Typ
========================================================= */

interface TeilnehmerOption {
  id: number;
  label: string;
}

interface Props {
  open: boolean;
  veranstaltungId: number;
  gruppeId: number;
  onClose: () => void;
  onSaved: () => void;
}

const TeilnehmerDialog: FC<Props> = ({ open, veranstaltungId, gruppeId, onClose, onSaved }) => {
  const [options, setOptions] = useState<TeilnehmerOption[]>([]);
  const [selected, setSelected] = useState<TeilnehmerOption[]>([]);
  const [loading, setLoading] = useState(false);

  /* =========================================================
     LOAD TEILNEHMER
  ========================================================= */

  useEffect(() => {
    if (!open) return;

    const load = async () => {
      try {
        setLoading(true);

        const res = await apiClient.get<PageResponse<TeilnehmerDTO>>(
          `/veranstaltungen/${veranstaltungId}/teilnehmer`,
        );

        const data = res.data.content;

        const mapped: TeilnehmerOption[] = data
          .filter((t) => !t.kuerzel)
          .map((t) => ({
            id: t.id,
            label: `${t.person.name}, ${t.person.vorname}`,
          }));

        setOptions(mapped);
      } catch (error: unknown) {
        if (axios.isAxiosError(error)) {
          console.error(error.response?.data?.message);
        }
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [open, veranstaltungId]);

  /* =========================================================
     SAVE BULK ASSIGN
  ========================================================= */

  const handleSave = async () => {
    try {
      await apiClient.put(
        `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer`,
        selected.map((s) => s.id),
      );

      onSaved();
      onClose();
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error(error.response?.data?.message);
      }
    }
  };

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Teilnehmer hinzufügen</DialogTitle>

      <DialogContent>
        <Autocomplete
          multiple
          options={options}
          getOptionLabel={(o) => o.label}
          value={selected}
          onChange={(_, value) => setSelected(value)}
          loading={loading}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Teilnehmer auswählen"
              InputProps={{
                ...params.InputProps,
                endAdornment: (
                  <>
                    {loading && <CircularProgress size={18} />}
                    {params.InputProps.endAdornment}
                  </>
                ),
              }}
            />
          )}
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleSave}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default TeilnehmerDialog;
