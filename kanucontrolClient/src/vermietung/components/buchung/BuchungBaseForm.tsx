// src/vermietung/components/buchung/BuchungBaseForm.tsx

import React, { useEffect, useState } from "react";
import { getPreis } from "@/vermietung/api/buchungApi";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import { Box, Checkbox, FormControlLabel, Paper, TextField, Typography } from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";

import FormFeld from "@/core/components/common/FormFeld";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { VereinAutocomplete } from "@/core/components/verein/VereinAutocomplete";

import type { Buchung } from "@/vermietung/types/Buchung";

import { BERECHNUNGSART_LABELS } from "@/vermietung/enums/Berechnungsart";

interface Props {
  form: BuchungSave;
  editMode: boolean;
  mietbereiche: Mietbereich[];
  mieter?: PersonRef;
  veranstalter?: VereinRef;
  buchungspositionen: Buchung["positionen"];
  createdAt?: string;
  buchungsquelle: Buchungsquelle;

  onMieterChange: (value?: PersonRef) => void;
  onVeranstalterChange: (value?: VereinRef) => void;

  onChange: <K extends keyof BuchungSave>(key: K, value: BuchungSave[K]) => void;
}

function useMietbereichPreise(
  mietbereichIds: number[],
  buchungspositionen: Buchung["positionen"],
  buchungsquelle: Buchungsquelle,
  stichtag: string,
) {
  const [preise, setPreise] = useState<Record<number, number>>({});
  const [ladefehler, setLadefehler] = useState<Record<number, boolean>>({});

  const mietbereichIdsKey = [...new Set(mietbereichIds)].sort((a, b) => a - b).join(",");

  useEffect(() => {
    let cancelled = false;

    const ids = mietbereichIdsKey ? mietbereichIdsKey.split(",").map(Number) : [];

    const gespeicherteIds = new Set(buchungspositionen.map((position) => position.mietbereichId));

    const fehlendeIds = ids.filter((id) => !gespeicherteIds.has(id) && preise[id] === undefined);

    if (fehlendeIds.length === 0) {
      return () => {
        cancelled = true;
      };
    }

    const ladePreise = async () => {
      const ergebnisse = await Promise.all(
        fehlendeIds.map(async (id) => {
          try {
            const preis = await getPreis(id, buchungsquelle, stichtag);
            return { id, preis, fehler: false };
          } catch (error) {
            console.error(`Preis für Mietbereich ${id} konnte nicht geladen werden:`, error);
            return { id, preis: undefined, fehler: true };
          }
        }),
      );

      if (cancelled) return;

      setPreise((aktuell) => {
        const neu = { ...aktuell };

        ergebnisse.forEach(({ id, preis }) => {
          if (preis !== undefined) neu[id] = preis;
        });

        return neu;
      });

      setLadefehler((aktuell) => {
        const neu = { ...aktuell };

        ergebnisse.forEach(({ id, fehler }) => {
          neu[id] = fehler;
        });

        return neu;
      });
    };

    void ladePreise();

    return () => {
      cancelled = true;
    };
  }, [mietbereichIdsKey, buchungspositionen, buchungsquelle, stichtag, preise]);

  return { preise, ladefehler };
}

const formatPreis = (preis?: number) =>
  preis == null
    ? "Preis nicht verfügbar"
    : new Intl.NumberFormat("de-DE", {
        style: "currency",
        currency: "EUR",
      }).format(preis);

export const BuchungBaseForm: React.FC<Props> = ({
  form,
  editMode,
  mietbereiche,
  mieter,
  veranstalter,
  onMieterChange,
  onVeranstalterChange,
  onChange,
  buchungspositionen,
  createdAt,
  buchungsquelle,
}) => {
  const gefilterteMietbereiche = mietbereiche.filter((mietbereich) =>
    form.buchungsquelle === "DIREKT" ? mietbereich.direktbuchungAktiv : mietbereich.airbnbAktiv,
  );

const stichtag = createdAt ? createdAt.slice(0, 10) : new Date().toLocaleDateString("sv-SE");

const { preise: geladenePreise, ladefehler } = useMietbereichPreise(
  form.positionen.map((position) => position.mietbereichId),
  buchungspositionen,
  buchungsquelle,
  stichtag,
);
  
  const gesamtpreis = form.positionen.reduce<number>((summe, position) => {
    const buchungsposition = buchungspositionen.find(
      (item) => item.mietbereichId === position.mietbereichId,
    );

    const preis = buchungsposition?.vereinbarterPreis ?? geladenePreise[position.mietbereichId];

    if (preis === undefined || ladefehler[position.mietbereichId]) {
      return summe;
    }

    return summe + preis * position.anzahl;
  }, 0);

  const gesamtpreisVollstaendig = form.positionen.every((position) => {
    const buchungsposition = buchungspositionen.find(
      (item) => item.mietbereichId === position.mietbereichId,
    );

    return (
      buchungsposition?.vereinbarterPreis !== undefined ||
      (geladenePreise[position.mietbereichId] !== undefined && !ladefehler[position.mietbereichId])
    );
  });
  
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
          <Box sx={{ mb: 2 }}>
            <Typography variant="subtitle2" color="text.secondary">
              Voraussichtliche Gesamteinnahmen
            </Typography>

            <Typography variant="h5" fontWeight={700} sx={{ mt: 0.5 }}>
              {gesamtpreisVollstaendig ? formatPreis(gesamtpreis) : "Preise werden ermittelt …"}
            </Typography>
          </Box>

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
                      <Box
                        sx={{
                          display: "flex",
                          alignItems: "center",
                          gap: 1.5,
                          flexWrap: "wrap",
                        }}
                      >
                        <TextField
                          label="Menge"
                          type="number"
                          size="small"
                          value={position.anzahl}
                          disabled={!editMode}
                          sx={{ width: 110, flexShrink: 0 }}
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

                        {(() => {
                          const buchungsposition = buchungspositionen.find(
                            (item) => item.mietbereichId === mietbereich.id,
                          );

                          const preis =
                            buchungsposition?.vereinbarterPreis ?? geladenePreise[mietbereich.id];

                          const berechnungsart =
                            buchungsposition?.berechnungsart ?? mietbereich.berechnungsart;

                          return (
                            <Box sx={{ minWidth: 110 }}>
                              <Typography variant="body2" fontWeight={600}>
                                {preis !== undefined
                                  ? formatPreis(preis * position.anzahl)
                                  : ladefehler[mietbereich.id]
                                    ? "Preis nicht verfügbar"
                                    : "Preis wird geladen …"}
                              </Typography>

                              {berechnungsart && (
                                <Typography variant="caption" color="text.secondary">
                                  {BERECHNUNGSART_LABELS[berechnungsart]}
                                </Typography>
                              )}
                            </Box>
                          );
                        })()}
                      </Box>
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
