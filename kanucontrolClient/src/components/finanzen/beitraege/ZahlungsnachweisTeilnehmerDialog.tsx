import {
  Autocomplete,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Button,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";

export interface TeilnehmerOption {
  id: number;
  vorname: string;
  nachname: string;
  hauptvereinAbk?: string;
}

interface TeilnehmerRefDTO {
  personId: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
}

interface Props {
  open: boolean;
  veranstaltungId: number;
  selectedIds: number[];
  onClose: () => void;
  onSave: (teilnehmer: TeilnehmerOption[]) => void;
}

const ZahlungsnachweisTeilnehmerDialog = ({
  open,
  veranstaltungId,
  selectedIds,
  onClose,
  onSave,
}: Props) => {
  const [options, setOptions] = useState<TeilnehmerOption[]>([]);
  const [selected, setSelected] = useState<TeilnehmerOption[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open || !veranstaltungId) return;

    const load = async () => {
      try {
        setLoading(true);

        const response = await apiClient.get<TeilnehmerRefDTO[]>(
          `/veranstaltungen/${veranstaltungId}/teilnehmer/search/ref`,
        );

        const mapped: TeilnehmerOption[] = response.data.map((t) => ({
          id: t.personId,
          vorname: t.vorname,
          nachname: t.name,
          hauptvereinAbk: t.hauptvereinAbk,
        }));

        setOptions(mapped);

        // Bereits ausgewählte Teilnehmer anzeigen
        setSelected(mapped.filter((t) => selectedIds.includes(t.id)));
      } catch (error) {
        console.error("Teilnehmer konnten nicht geladen werden", error);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [open, veranstaltungId, selectedIds]);

  const handleSave = () => {
    onSave(selected);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Teilnehmer hinzufügen</DialogTitle>

      <DialogContent>
        <Autocomplete
          multiple
          options={options}
          value={selected}
          onChange={(_, value) => setSelected(value)}
          getOptionLabel={(option) => `${option.nachname}, ${option.vorname}`}
          isOptionEqualToValue={(option, value) => option.id === value.id}
          filterSelectedOptions
          loading={loading}
          renderOption={(props, option) => (
            <li {...props} key={option.id}>
              <div>
                <Typography>
                  {option.nachname}, {option.vorname}
                </Typography>

                {option.hauptvereinAbk && (
                  <Typography variant="caption" color="text.secondary">
                    {option.hauptvereinAbk}
                  </Typography>
                )}
              </div>
            </li>
          )}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Teilnehmer auswählen"
              placeholder="Name eingeben ..."
              InputProps={{
                ...params.InputProps,
                endAdornment: (
                  <>
                    {loading && <CircularProgress size={20} />}

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
          Übernehmen
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ZahlungsnachweisTeilnehmerDialog;
