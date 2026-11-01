// src/vermietung/components/buchung/BuchungBaseForm.tsx

import React, { useEffect, useState } from "react";
import { getPreis } from "@/vermietung/api/buchungApi";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import {
  Alert,
  Box,
  Checkbox,
  FormControlLabel,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";

import FormFeld from "@/core/components/common/FormFeld";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { VereinAutocomplete } from "@/core/components/verein/VereinAutocomplete";
import type { VerbrauchskostenVorschau } from "@/vermietung/types/VerbrauchskostenVorschau";
import type { Buchung } from "@/vermietung/types/Buchung";
import { BERECHNUNGSART_LABELS } from "@/vermietung/enums/Berechnungsart";
import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";

interface Props {
  form: BuchungSave;
  neueBuchung: boolean;
  editMode: boolean;
  mietbereiche: Mietbereich[];
  rabatteByMietbereich: Record<number, MietbereichRabatt[]>;
  mieter?: PersonRef;
  veranstalter?: VereinRef;
  buchungspositionen: Buchung["positionen"];
  createdAt?: string | null;
  buchungsquelle: Buchungsquelle;
  buchungsdatenEditierbar: boolean;
  verbrauchskosten: Buchung["verbrauchskosten"];
  buchungsstatus?: Buchung["status"];
  verbrauchskostenVorschau: VerbrauchskostenVorschau[];

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

const formatZahl = (wert?: number | null, nachkommastellen = 3) =>
  wert == null
    ? "–"
    : new Intl.NumberFormat("de-DE", {
        maximumFractionDigits: nachkommastellen,
      }).format(wert);

const formatDatumZeit = (wert?: string | null) => {
  if (!wert) return "–";

  const datum = new Date(wert);

  if (Number.isNaN(datum.getTime())) return wert;

  return new Intl.DateTimeFormat("de-DE", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(datum);
};

export const BuchungBaseForm: React.FC<Props> = ({
  form,
  neueBuchung,
  editMode,
  buchungsdatenEditierbar,
  mietbereiche,
  rabatteByMietbereich,
  mieter,
  veranstalter,
  onMieterChange,
  onVeranstalterChange,
  onChange,
  buchungspositionen,
  createdAt,
  buchungsquelle,
  verbrauchskosten,
  verbrauchskostenVorschau,
  buchungsstatus,
}) => {
  const [voraussetzungsFehler, setVoraussetzungsFehler] = useState<string | null>(null);
  const gefilterteMietbereiche = mietbereiche.filter((mietbereich) =>
    form.buchungsquelle === "DIREKT" ? mietbereich.direktbuchungAktiv : mietbereich.airbnbAktiv,
  );
  const voraussetzungenFuer = (bereich: Mietbereich) => form.buchungsquelle === "DIREKT"
    ? bereich.erforderlicheMietbereichIdsDirekt ?? bereich.erforderlicheMietbereichIds ?? []
    : bereich.erforderlicheMietbereichIdsAirbnb ?? [];

  const stichtag = createdAt ? createdAt.slice(0, 10) : new Date().toLocaleDateString("sv-SE");

  const { mietobjekt, loading: mietobjektLoading } = useVermietungContext();

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

  const rabattbetrag = (() => {
    if (form.buchungsquelle === "AIRBNB") return 0;
    if (form.rabattart === Rabattart.INDIVIDUELL && form.rabattwertTyp === RabattwertTyp.ABSOLUT) {
      return Math.min(gesamtpreis, Math.max(0, form.rabattwert ?? 0));
    }

    return form.positionen.reduce((summe, position) => {
      const areaPrice = buchungspositionen.find((item) => item.mietbereichId === position.mietbereichId)?.vereinbarterPreis
        ?? geladenePreise[position.mietbereichId];
      if (areaPrice === undefined || ladefehler[position.mietbereichId]) return summe;

      const positionspreis = areaPrice * position.anzahl;
      const aktiveRabatte = (rabatteByMietbereich[position.mietbereichId] ?? []).filter((rabatt) => rabatt.aktiv);
      let regel: MietbereichRabatt | undefined;
      let wertTyp: RabattwertTyp | null = null;
      let wert: number | null = null;

      if (form.rabattart === Rabattart.INDIVIDUELL && form.rabattwertTyp === RabattwertTyp.PROZENTUAL) {
        if (aktiveRabatte.length > 0) {
          wertTyp = RabattwertTyp.PROZENTUAL;
          wert = form.rabattwert ?? 0;
        }
      } else if (form.rabattart === Rabattart.VEREIN || form.rabattart === Rabattart.VERBAND) {
        regel = aktiveRabatte.find((rabatt) => rabatt.rabattart === form.rabattart);
        wertTyp = regel?.rabattwertTyp ?? null;
        wert = regel?.rabattwert ?? null;
      }

      if (wertTyp === RabattwertTyp.PROZENTUAL && wert != null) {
        return summe + Math.min(positionspreis, positionspreis * wert / 100);
      }
      if (wertTyp === RabattwertTyp.ABSOLUT && wert != null) {
        return summe + Math.min(positionspreis, wert);
      }
      return summe;
    }, 0);
  })();

  const mietpreisNachRabatt = Math.max(0, gesamtpreis - rabattbetrag);
  const standardzeitenFehlen = Boolean(
    !mietobjekt?.anreisezeit || (!form.unbefristet && !mietobjekt?.abreisezeit),
  );

  const findeVoraussetzungen = (bereichId: number, besucht = new Set<number>()): number[] | null => {
    if (besucht.has(bereichId)) return [];
    besucht.add(bereichId);
    const bereich = gefilterteMietbereiche.find((item) => item.id === bereichId);
    if (!bereich) return null;
    const ergebnis = new Set<number>();
    for (const voraussetzungId of voraussetzungenFuer(bereich)) {
      const weitere = findeVoraussetzungen(voraussetzungId, besucht);
      if (weitere === null) return null;
      ergebnis.add(voraussetzungId);
      weitere.forEach((id) => ergebnis.add(id));
    }
    return [...ergebnis];
  };

  const aendereMietbereichAuswahl = (bereichId: number, ausgewaehlt: boolean) => {
    setVoraussetzungsFehler(null);
    if (ausgewaehlt) {
      const voraussetzungen = findeVoraussetzungen(bereichId);
      if (voraussetzungen === null) {
        setVoraussetzungsFehler("Ein benötigter Mietbereich ist für diese Buchungsquelle nicht verfügbar.");
        return;
      }
      const ids = new Set([...form.positionen.map((position) => position.mietbereichId), bereichId, ...voraussetzungen]);
      onChange("positionen", [...ids].map((id) =>
        form.positionen.find((position) => position.mietbereichId === id) ?? { mietbereichId: id, anzahl: 1 },
      ));
      return;
    }

    const zuEntfernendeIds = new Set<number>([bereichId]);
    let erweitert = true;
    while (erweitert) {
      erweitert = false;
      for (const position of form.positionen) {
        if (zuEntfernendeIds.has(position.mietbereichId)) continue;
        const mietbereich = gefilterteMietbereiche.find((item) => item.id === position.mietbereichId);
        if (mietbereich && voraussetzungenFuer(mietbereich).some((id) => zuEntfernendeIds.has(id))) {
          zuEntfernendeIds.add(position.mietbereichId);
          erweitert = true;
        }
      }
    }
    onChange("positionen", form.positionen.filter((position) => !zuEntfernendeIds.has(position.mietbereichId)));
  };

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
          disabled={!buchungsdatenEditierbar}
          onChange={onMieterChange}
        />

        <VereinAutocomplete
          label="Veranstalter"
          value={veranstalter}
          disabled={!buchungsdatenEditierbar}
          onChange={onVeranstalterChange}
        />

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "2fr 1fr" },
            gap: 2,
          }}
        >
          {neueBuchung && buchungsdatenEditierbar && mietobjektLoading && (
            <Alert severity="info" sx={{ gridColumn: "1 / -1" }}>
              Das aktive Mietobjekt und seine Standardzeiten werden geladen.
            </Alert>
          )}
          {neueBuchung && buchungsdatenEditierbar && !mietobjektLoading && !mietobjekt && (
            <Alert severity="error" sx={{ gridColumn: "1 / -1" }}>
              Es ist kein aktives Mietobjekt ausgewählt. Eine Buchung ist erst möglich, wenn ein Mietobjekt aktiviert ist.
            </Alert>
          )}
          {neueBuchung && buchungsdatenEditierbar && !mietobjektLoading && mietobjekt && standardzeitenFehlen && (
            <Alert severity="warning" sx={{ gridColumn: "1 / -1" }}>
              Für dieses Mietobjekt fehlen Standardzeiten. Bitte hinterlege die Standard-Anreisezeit
              {!form.unbefristet && " und Standard-Abreisezeit"} unter VERMIETUNG → Mietobjekte.
            </Alert>
          )}
          <FormFeld
            label="Beginn"
            type="date"
            value={form.anreise?.slice(0, 10) ?? ""}
            disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.anreisezeit))}
            slotProps={{ inputLabel: { shrink: true } }}
            onChange={(datum) => {
              if (!datum) {
                onChange("anreise", "");
                return;
              }

              const bisherigeZeit = form.anreise?.slice(11, 16);
              const standardzeit = mietobjekt?.anreisezeit;

              const zeit = bisherigeZeit || standardzeit;

              onChange(
                "anreise",
                zeit ? `${datum}T${zeit.length === 5 ? `${zeit}:00` : zeit}` : "",
              );
            }}
          />

          <TextField
            label="Uhrzeit"
            type="time"
            value={form.anreise?.slice(11, 16) ?? ""}
            disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.anreisezeit))}
            fullWidth
            size="small"
            slotProps={{
              inputLabel: { shrink: true },
              htmlInput: { step: 300 },
            }}
            onChange={(event) => {
              const zeit = event.target.value;
              const datum = form.anreise?.slice(0, 10) ?? "";

              onChange("anreise", datum && zeit ? `${datum}T${zeit}:00` : "");
            }}
          />
        </Box>

        <FormControlLabel
          control={
            <Checkbox
              checked={form.unbefristet ?? false}
              disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.abreisezeit))}
              onChange={(event) => onChange("unbefristet", event.target.checked)}
            />
          }
          label="Buchung ohne Enddatum"
        />

        {!form.unbefristet && (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", sm: "2fr 1fr" },
              gap: 2,
            }}
          >
            <FormFeld
              label="Ende"
              type="date"
              value={form.abreise?.slice(0, 10) ?? ""}
              disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.abreisezeit))}
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={(datum) => {
                if (!datum) {
                  onChange("abreise", "");
                  return;
                }

                const bisherigeZeit = form.abreise?.slice(11, 16);
                const standardzeit = mietobjekt?.abreisezeit;

                const zeit = bisherigeZeit || standardzeit;

                onChange(
                  "abreise",
                  zeit ? `${datum}T${zeit.length === 5 ? `${zeit}:00` : zeit}` : "",
                );
              }}
            />

            <TextField
              label="Uhrzeit"
              type="time"
              value={form.abreise?.slice(11, 16) ?? ""}
              disabled={!buchungsdatenEditierbar}
              fullWidth
              size="small"
              slotProps={{
                inputLabel: { shrink: true },
                htmlInput: { step: 300 },
              }}
              onChange={(event) => {
                const zeit = event.target.value;
                const datum = form.abreise?.slice(0, 10) ?? "";

                onChange("abreise", datum && zeit ? `${datum}T${zeit}:00` : "");
              }}
            />
          </Box>
        )}

        <TextField
          label="Bemerkung"
          value={form.bemerkung ?? ""}
          disabled={!editMode}
          multiline
          minRows={3}
          fullWidth
          onChange={(event) => onChange("bemerkung", event.target.value)}
        />

        <TextField
          select
          label="Bewertung des Mieters"
          value={form.bewertung ?? ""}
          disabled={!editMode}
          fullWidth
          onChange={(event) => {
            const value = event.target.value;
            onChange("bewertung", value === "" ? null : Number(value));
          }}
          helperText="Optionale Bewertung von 1 bis 5 Sternen"
        >
          <MenuItem value="">Keine Bewertung</MenuItem>
          {[1, 2, 3, 4, 5].map((wert) => (
            <MenuItem key={wert} value={wert}>
              {"★".repeat(wert)}
              {"☆".repeat(5 - wert)} ({wert}/5)
            </MenuItem>
          ))}
        </TextField>

        {form.buchungsquelle === "AIRBNB" ? (
          <Alert severity="info">Für Airbnb-Buchungen ist keine Rabattierung vorgesehen.</Alert>
        ) : (
          <TextField
            select
            label="Rabatt"
            value={form.rabattart ?? Rabattart.KEIN_RABATT}
            disabled={!buchungsdatenEditierbar}
            fullWidth
            onChange={(event) => onChange("rabattart", event.target.value as Rabattart)}
          >
            <MenuItem value={Rabattart.KEIN_RABATT}>Kein Rabatt</MenuItem>
            <MenuItem value={Rabattart.VEREIN}>Vereinsrabatt</MenuItem>
            <MenuItem value={Rabattart.VERBAND}>Verbandsrabatt</MenuItem>
            <MenuItem value={Rabattart.INDIVIDUELL}>Individueller Rabatt</MenuItem>
          </TextField>
        )}

        {form.buchungsquelle !== "AIRBNB" && form.rabattart === Rabattart.INDIVIDUELL && (
          <Box sx={{ display: "grid", gridTemplateColumns: "minmax(120px, 1fr) minmax(110px, 1fr)", gap: 1 }}>
            <TextField
              select
              label="Rabattart"
              value={form.rabattwertTyp ?? RabattwertTyp.PROZENTUAL}
              disabled={!buchungsdatenEditierbar}
              onChange={(event) => onChange("rabattwertTyp", event.target.value as RabattwertTyp)}
            >
              <MenuItem value={RabattwertTyp.PROZENTUAL}>Prozent</MenuItem>
              <MenuItem value={RabattwertTyp.ABSOLUT}>Euro</MenuItem>
            </TextField>
            <TextField
              label={form.rabattwertTyp === RabattwertTyp.PROZENTUAL ? "Prozent" : "Betrag (€)"}
              type="number"
              value={form.rabattwert ?? 0}
              disabled={!buchungsdatenEditierbar}
              slotProps={{ htmlInput: { min: 0, ...(form.rabattwertTyp === RabattwertTyp.PROZENTUAL ? { max: 100 } : {}), step: "0.01" }}}
              onChange={(event) => onChange("rabattwert", event.target.value === "" ? 0 : Number(event.target.value))}
            />
          </Box>
        )}
      </Box>

      {/* Mittlere Spalte: Mietbereiche und Positionen */}
      <Box sx={{ minWidth: 0 }}>
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            borderRadius: 1,
          }}
        >
          <Box sx={{ mb: 2 }}>
            {form.buchungsquelle === "AIRBNB" ? (
              <>
                <Typography variant="subtitle2" color="text.secondary">Mietpreis</Typography>
                <Typography variant="h5" fontWeight={700} sx={{ mt: 0.5 }}>
                  {gesamtpreisVollstaendig ? formatPreis(gesamtpreis) : "Preise werden ermittelt …"}
                </Typography>
              </>
            ) : (
              <>
                <Typography variant="subtitle2" color="text.secondary">Mietpreis vor Rabatt</Typography>
                <Typography variant="h6" fontWeight={600} sx={{ mt: 0.5 }}>
                  {gesamtpreisVollstaendig ? formatPreis(gesamtpreis) : "Preise werden ermittelt …"}
                </Typography>
                {rabattbetrag > 0 && (
                  <>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>Rabatt</Typography>
                    <Typography variant="body1" color="success.main">− {formatPreis(rabattbetrag)}</Typography>
                  </>
                )}
                <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 0.75 }}>Mietpreis nach Rabatt</Typography>
                <Typography variant="h5" fontWeight={700} sx={{ mt: 0.5 }}>
                  {gesamtpreisVollstaendig ? formatPreis(mietpreisNachRabatt) : "Preise werden ermittelt …"}
                </Typography>
                <Typography variant="caption" color="text.secondary">Verbrauchskosten sind nicht rabattiert.</Typography>
              </>
            )}
          </Box>

          <Typography variant="subtitle1" fontWeight={600} color="text.secondary" sx={{ mb: 1.5 }}>
            Mietbereiche / Positionen
          </Typography>

          {voraussetzungsFehler && <Alert severity="error" sx={{ mb: 1.5 }}>{voraussetzungsFehler}</Alert>}

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
                          disabled={!buchungsdatenEditierbar}
                          onChange={(event) => {
                            aendereMietbereichAuswahl(mietbereich.id, event.target.checked);
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

                          {voraussetzungenFuer(mietbereich).length > 0 && (
                            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
                              Erfordert: {gefilterteMietbereiche
                                .filter((item) => voraussetzungenFuer(mietbereich).includes(item.id))
                                .map((item) => item.bezeichnung)
                                .join(", ") || "einen anderen Mietbereich"}
                            </Typography>
                          )}

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
                          disabled={!buchungsdatenEditierbar}
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
      {/* Dritte Spalte: Verbrauchswerte und Kosten */}
      <Box sx={{ minWidth: 0 }}>
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            borderRadius: 1,
          }}
        >
          <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
            Verbrauchswerte und Kosten
          </Typography>

          {form.buchungsquelle === "AIRBNB" ? (
            <Typography variant="body2" color="text.secondary">
              Für Airbnb-Buchungen ist keine Zählerstandsprüfung beim Abschluss erforderlich.
            </Typography>
          ) : buchungsstatus === "ABRECHENBAR" ? (
            !verbrauchskostenVorschau || verbrauchskostenVorschau.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                Es liegen keine Verbrauchskosten für die Vorschau vor.
              </Typography>
            ) : (
              <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                {verbrauchskostenVorschau.map((kosten, index) => (
                  <Box
                    key={`${kosten.zaehlerId ?? "pauschal"}-${kosten.bezeichnung}-${index}`}
                    sx={{
                      pb: 2,
                      borderBottom: "1px solid",
                      borderColor: "divider",
                      "&:last-child": {
                        pb: 0,
                        borderBottom: 0,
                      },
                    }}
                  >
                    <Typography variant="subtitle2" fontWeight={600}>
                      {kosten.bezeichnung}
                      {kosten.zaehlerBezeichnung ? ` – ${kosten.zaehlerBezeichnung}` : ""}
                    </Typography>

                    {!kosten.erfolgreich && (
                      <Typography
                        variant="body2"
                        color="error"
                        sx={{ mt: 1, overflowWrap: "anywhere" }}
                      >
                        {kosten.hinweis ?? "Die Verbrauchskosten konnten nicht berechnet werden."}
                      </Typography>
                    )}

                    {kosten.erfolgreich && (
                      <>
                        {kosten.abrechnungsart === "ZAEHLERSTAND" ? (
                          <Box
                            sx={{
                              display: "grid",
                              gridTemplateColumns: "1fr auto",
                              columnGap: 1,
                              rowGap: 0.75,
                              mt: 1,
                            }}
                          >
                            <Typography variant="body2" color="text.secondary">
                              Anfangsstand
                            </Typography>
                            <Typography variant="body2" textAlign="right">
                              {formatZahl(kosten.anfangsstand)}
                              {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                            </Typography>

                            <Typography variant="caption" color="text.secondary">
                              {formatDatumZeit(kosten.anfangsablesung)}
                            </Typography>
                            <Box />

                            <Typography variant="body2" color="text.secondary">
                              Endstand
                            </Typography>
                            <Typography variant="body2" textAlign="right">
                              {formatZahl(kosten.endstand)}
                              {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                            </Typography>

                            <Typography variant="caption" color="text.secondary">
                              {formatDatumZeit(kosten.endablesung)}
                            </Typography>
                            <Box />

                            <Typography variant="body2" color="text.secondary">
                              Verbrauch
                            </Typography>
                            <Typography variant="body2" fontWeight={600} textAlign="right">
                              {formatZahl(kosten.verbrauch)}
                              {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                            </Typography>

                            <Typography variant="body2" color="text.secondary">
                              Preis je Einheit
                            </Typography>
                            <Typography variant="body2" textAlign="right">
                              {formatPreis(kosten.preis ?? undefined)}
                            </Typography>
                          </Box>
                        ) : kosten.abrechnungsart === "TAGESPREIS" ? (
                          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            {formatZahl(kosten.abrechnungstage, 0)} Tage ×{" "}
                            {formatPreis(kosten.preis ?? undefined)}
                          </Typography>
                        ) : (
                          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            Pauschalbetrag
                          </Typography>
                        )}

                        <Box
                          sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "baseline",
                            gap: 1,
                            mt: 1.5,
                          }}
                        >
                          <Typography variant="body2" color="text.secondary">
                            Kosten
                          </Typography>
                          <Typography variant="subtitle1" fontWeight={700}>
                            {formatPreis(kosten.kosten ?? undefined)}
                          </Typography>
                        </Box>
                      </>
                    )}
                  </Box>
                ))}

                {verbrauchskostenVorschau.every((kosten) => kosten.erfolgreich) && (
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "baseline",
                      gap: 1,
                      pt: 1,
                      borderTop: "2px solid",
                      borderColor: "divider",
                    }}
                  >
                    <Typography variant="subtitle1" fontWeight={600}>
                      Verbrauchskosten gesamt
                    </Typography>

                    <Typography variant="h6" fontWeight={700}>
                      {formatPreis(
                        verbrauchskostenVorschau.reduce(
                          (summe, kosten) => summe + (kosten.kosten ?? 0),
                          0,
                        ),
                      )}
                    </Typography>
                  </Box>
                )}
              </Box>
            )
          ) : buchungsstatus !== "ABGESCHLOSSEN" ? (
            <Typography variant="body2" color="text.secondary">
              Die Verbrauchskosten werden nach Ende des Buchungszeitraums in der Vorschau berechnet.
              Voraussetzung sind die erforderlichen Zählerstände innerhalb des zulässigen
              Zeitfensters.
            </Typography>
          ) : !verbrauchskosten || verbrauchskosten.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Für diese Buchung sind keine Verbrauchskosten gespeichert.
            </Typography>
          ) : (
            <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
              {verbrauchskosten.map((kosten) => (
                <Box
                  key={kosten.id}
                  sx={{
                    pb: 2,
                    borderBottom: "1px solid",
                    borderColor: "divider",
                    "&:last-child": {
                      pb: 0,
                      borderBottom: 0,
                    },
                  }}
                >
                  <Typography variant="subtitle2" fontWeight={600}>
                    {kosten.bezeichnung}
                  </Typography>

                  {kosten.abrechnungsart === "ZAEHLERSTAND" ? (
                    <Box sx={{ mt: 1 }}>
                      <Box
                        sx={{
                          display: "grid",
                          gridTemplateColumns: "1fr auto",
                          columnGap: 1,
                          rowGap: 0.75,
                        }}
                      >
                        <Typography variant="body2" color="text.secondary">
                          Anfangsstand
                        </Typography>
                        <Typography variant="body2" textAlign="right">
                          {formatZahl(kosten.anfangsstand)}
                          {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                        </Typography>

                        <Typography variant="caption" color="text.secondary">
                          {formatDatumZeit(kosten.anfangsablesung)}
                        </Typography>
                        <Box />

                        <Typography variant="body2" color="text.secondary">
                          Endstand
                        </Typography>
                        <Typography variant="body2" textAlign="right">
                          {formatZahl(kosten.endstand)}
                          {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                        </Typography>

                        <Typography variant="caption" color="text.secondary">
                          {formatDatumZeit(kosten.endablesung)}
                        </Typography>
                        <Box />

                        <Typography variant="body2" color="text.secondary">
                          Verbrauch
                        </Typography>
                        <Typography variant="body2" fontWeight={600} textAlign="right">
                          {formatZahl(kosten.verbrauch)}
                          {kosten.mengeneinheit ? ` ${kosten.mengeneinheit}` : ""}
                        </Typography>

                        <Typography variant="body2" color="text.secondary">
                          Preis je Einheit
                        </Typography>
                        <Typography variant="body2" textAlign="right">
                          {formatPreis(kosten.preis)}
                        </Typography>
                      </Box>
                    </Box>
                  ) : kosten.abrechnungsart === "TAGESPREIS" ? (
                    <Box
                      sx={{
                        display: "flex",
                        justifyContent: "space-between",
                        gap: 1,
                        mt: 1,
                      }}
                    >
                      <Typography variant="body2" color="text.secondary">
                        {formatZahl(kosten.abrechnungstage, 0)} Tage × {formatPreis(kosten.preis)}
                      </Typography>
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                      Pauschalbetrag
                    </Typography>
                  )}

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "baseline",
                      gap: 1,
                      mt: 1.5,
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      Kosten
                    </Typography>

                    <Typography variant="subtitle1" fontWeight={700}>
                      {formatPreis(kosten.kosten)}
                    </Typography>
                  </Box>
                </Box>
              ))}

              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "baseline",
                  gap: 1,
                  pt: 1,
                  borderTop: "2px solid",
                  borderColor: "divider",
                }}
              >
                <Typography variant="subtitle1" fontWeight={600}>
                  Verbrauchskosten gesamt
                </Typography>

                <Typography variant="h6" fontWeight={700}>
                  {formatPreis(
                    verbrauchskosten.reduce((summe, kosten) => summe + kosten.kosten, 0),
                  )}
                </Typography>
              </Box>
            </Box>
          )}
        </Paper>
      </Box>
    </>
  );
};
