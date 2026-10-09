// src/vermietung/components/buchung/BuchungBaseForm.tsx

import React from "react";
import { Box, Checkbox, FormControlLabel, Paper, TextField, Typography } from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";

import FormFeld from "@/core/components/common/FormFeld";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { VereinAutocomplete } from "@/core/components/verein/VereinAutocomplete";

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
  const gefilterteMietbereiche = mietbereiche.filter((mietbereich) =>
    form.buchungsquelle === "DIREKT" ? mietbereich.direktbuchungAktiv : mietbereich.airbnbAktiv,
  );

  return (
    <>
      {/* Linke Spalte: Buchungsdaten */}
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          gap: 2,
          minWidth: 0,
        }}
      >
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
          label="Beginn"
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

        <FormControlLabel
          control={
            <Checkbox
              checked={form.unbefristet ?? false}
              disabled={!editMode}
              onChange={(event) => onChange("unbefristet", event.target.checked)}
            />
          }
          label="Buchung ohne Enddatum"
        />

        {!form.unbefristet && (
          <FormFeld
            label="Ende"
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
                ? "Das Ende muss am selben Tag oder später als der Beginn liegen."
                : undefined
            }
            onChange={(v) => onChange("abreise", v)}
          />
        )}
      </Box>

      {/* Rechte Spalte: Mietbereiche und Positionen */}
      <Box sx={{ minWidth: 0 }}>
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            borderRadius: 1,
          }}
        >
          <Typography variant="subtitle1" fontWeight={600} color="text.secondary" sx={{ mb: 1.5 }}>
            Mietbereiche / Positionen
          </Typography>

          {gefilterteMietbereiche.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Keine Mietbereiche für diese Buchungsquelle verfügbar.
            </Typography>
          ) : (
            <Box
              sx={{
                display: "flex",
                flexDirection: "column",
                gap: 1.5,
              }}
            >
              {gefilterteMietbereiche.map((mietbereich) => {
                const position = form.positionen.find(
                  (item) => item.mietbereichId === mietbereich.id,
                );

                const selected = Boolean(position);

                return (
                  <Box
                    key={mietbereich.id}
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      gap: 1,
                      flexWrap: "wrap",
                      pb: 1,
                      borderBottom: "1px solid",
                      borderColor: "divider",
                      "&:last-child": {
                        borderBottom: 0,
                        pb: 0,
                      },
                    }}
                  >
                    <FormControlLabel
                      sx={{
                        m: 0,
                        flex: "1 1 180px",
                        minWidth: 0,
                      }}
                      control={
                        <Checkbox
                          size="small"
                          checked={selected}
                          disabled={!editMode}
                          onChange={(event) => {
                            const checked = event.target.checked;

                            const positionen = checked
                              ? [
                                  ...form.positionen,
                                  {
                                    mietbereichId: mietbereich.id,
                                    anzahl: 1,
                                  },
                                ]
                              : form.positionen.filter(
                                  (item) => item.mietbereichId !== mietbereich.id,
                                );

                            onChange("positionen", positionen);
                          }}
                        />
                      }
                      label={
                        <Box>
                          <Typography
                            variant="body2"
                            fontWeight={500}
                            sx={{ overflowWrap: "anywhere" }}
                          >
                            {mietbereich.bezeichnung}
                          </Typography>

                          <Typography variant="caption" color="text.secondary">
                            Bestand: {mietbereich.bestand}
                            {mietbereich.mengeneinheit ? ` ${mietbereich.mengeneinheit}` : ""}
                          </Typography>
                        </Box>
                      }
                    />

                    {position && (
                      <TextField
                        label="Menge"
                        type="number"
                        size="small"
                        value={position.anzahl}
                        disabled={!editMode}
                        sx={{
                          width: 110,
                          flexShrink: 0,
                        }}
                        slotProps={{
                          htmlInput: {
                            min: 1,
                            max: mietbereich.bestand,
                            step: 1,
                          },
                        }}
                        onChange={(event) => {
                          const rawValue = event.target.value;
                          const anzahl = Number(rawValue);

                          if (
                            rawValue === "" ||
                            !Number.isInteger(anzahl) ||
                            anzahl < 1 ||
                            anzahl > mietbereich.bestand
                          ) {
                            return;
                          }

                          const positionen = form.positionen.map((item) =>
                            item.mietbereichId === mietbereich.id ? { ...item, anzahl } : item,
                          );

                          onChange("positionen", positionen);
                        }}
                      />
                    )}
                  </Box>
                );
              })}
            </Box>
          )}
        </Paper>
      </Box>
    </>
  );
};
