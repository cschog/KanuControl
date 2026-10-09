import React from "react";
import { Box, Checkbox, FormControlLabel, FormGroup, Paper } from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import FormFeld from "@/core/components/common/FormFeld";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { VereinAutocomplete } from "@/core/components/verein/VereinAutocomplete";

import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";

import type { BuchungSave } from "@/vermietung/types/BuchungSave";

interface Props {
  form: BuchungSave;
  editMode: boolean;
  mietbereiche: Mietbereich[];
  mieter?: PersonRef;
  veranstalter?: VereinRef;

  onMieterChange: (value?: PersonRef) => void;
  onVeranstalterChange: (value?: VereinRef) => void;

  onChange: <K extends keyof BuchungSave>(key: K, value: BuchungSave[K]) => void;
}

export const BuchungBaseForm: React.FC<Props> = ({
  form,
  editMode,
  mietbereiche,
  mieter,
  veranstalter,
  onMieterChange,
  onVeranstalterChange,
  onChange,
}) => {
  return (
    <>
      <PersonAutocomplete
        label="Mieter"
        value={mieter}
        disabled={!editMode}
        onChange={onMieterChange}
      />

      <VereinAutocomplete
        label="Veranstalter"
        value={veranstalter}
        disabled={!editMode}
        onChange={onVeranstalterChange}
      />

      <FormFeld
        label="Anreise"
        type="date"
        value={form.anreise}
        disabled={!editMode}
        slotProps={{
          inputLabel: {
            shrink: true,
          },
        }}
        onChange={(v) => onChange("anreise", v)}
      />

      <FormFeld
        label="Abreise"
        type="date"
        value={form.abreise}
        disabled={!editMode}
        slotProps={{
          inputLabel: {
            shrink: true,
          },
        }}
        inputProps={{
          min: form.anreise || undefined,
        }}
        dataStatus={
          form.anreise && form.abreise && form.abreise < form.anreise ? "ERROR" : undefined
        }
        dataStatusMessage={
          form.anreise && form.abreise && form.abreise < form.anreise
            ? "Die Abreise muss am selben Tag oder später als die Anreise liegen."
            : undefined
        }
        onChange={(v) => onChange("abreise", v)}
      />

      {mietbereiche.length > 0 && (
        <Paper
          variant="outlined"
          sx={{
            gridColumn: {
              xs: "1",
              sm: "1 / -1",
            },
            p: 1.5,
            borderRadius: 1,
          }}
        >
          <Box
            sx={{
              fontSize: "0.875rem",
              fontWeight: 600,
              color: "text.secondary",
              mb: 0.5,
            }}
          >
            Mietbereiche / Positionen
          </Box>

          <FormGroup
            row
            sx={{
              columnGap: 1.5,
              rowGap: 0,
              "& .MuiFormControlLabel-root": {
                mr: 1,
                my: 0,
              },
            }}
          >
            {mietbereiche.map((mietbereich) => (
              <FormControlLabel
                key={mietbereich.id}
                control={
                  <Checkbox
                    size="small"
                    checked={form.mietbereichIds.includes(mietbereich.id)}
                    disabled={!editMode}
                    onChange={(event) => {
                      const selected = event.target.checked;

                      const mietbereichIds = selected
                        ? [...form.mietbereichIds, mietbereich.id]
                        : form.mietbereichIds.filter((id) => id !== mietbereich.id);

                      onChange("mietbereichIds", mietbereichIds);
                    }}
                  />
                }
                label={mietbereich.bezeichnung}
              />
            ))}
          </FormGroup>
        </Paper>
      )}
    </>
  );
};
