import React, { useEffect, useState } from "react";
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

import apiClient from "@/api/client/apiClient";
import { PersonRef } from "@/api/types/PersonRef";
import { Page } from "@/api/types/Page";

interface Props {
  open: boolean;
  onClose: () => void;
  onSelect: (person: PersonRef | undefined) => void;
}

export const KontoinhaberSelectDialog: React.FC<Props> = ({ open, onClose, onSelect }) => {
  const [options, setOptions] = useState<PersonRef[]>([]);
  const [value, setValue] = useState<PersonRef | null>(null);
  const [inputValue, setInputValue] = useState("");
  const [loading, setLoading] = useState(false);

  // 🔍 Remote Search
  useEffect(() => {
    if (!open || inputValue.length < 2) {
      setOptions([]);
      return;
    }

    let active = true;
    setLoading(true);

    apiClient
      .get<Page<PersonRef>>("/person/search", {
        params: {
          name: inputValue,
          size: 10,
          sort: "name,asc",
        },
      })
      .then((res) => {
        if (active) {
          setOptions(res.data.content);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [inputValue, open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Kontoinhaber auswählen</DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        <Autocomplete<PersonRef>
          value={value}
          options={options}
          loading={loading}
          filterOptions={(x) => x} // ❗ wichtig: kein Client-Filter
          getOptionLabel={(p) => `${p.vorname} ${p.name}`}
          isOptionEqualToValue={(a, b) => a.id === b.id}
          onChange={(_, newValue) => setValue(newValue)}
          onInputChange={(_, newInput) => setInputValue(newInput)}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Kontoinhaber"
              placeholder="Name oder Vorname eingeben…"
              InputProps={{
                ...params.InputProps,
                endAdornment: (
                  <>
                    {loading ? <CircularProgress size={18} /> : null}
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
        <Button
          variant="contained"
          disabled={!value}
          onClick={() => {
            onSelect(value ?? undefined);
            onClose();
          }}
        >
          Übernehmen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
