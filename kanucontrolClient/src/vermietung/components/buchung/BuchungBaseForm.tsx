import { formatPersonName } from "@/core/utils/personName";
// src/vermietung/components/buchung/BuchungBaseForm.tsx

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import EntschaedigungEditor from "./EntschaedigungEditor";
import { getVerbrauchskosten } from "@/vermietung/api/verbrauchskostenApi";
import type { Verbrauchskosten } from "@/vermietung/types/Verbrauchskosten";
import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";
import type { BuchungBereich } from "./buchungBereiche";
import { BuchungBankverbindung } from "./BuchungBankverbindung";
import React, { useEffect, useMemo, useState } from "react";
import { geldRunden, mietEinheiten } from "@/vermietung/utils/mietpreis";
import { getPreis } from "@/vermietung/api/buchungApi";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Button,
  Alert,
  Box,
  Checkbox,
  FormControlLabel,
  MenuItem,
  TextField,
  Typography,
} from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";

import { FormFeldDatePicker } from "@/core/components/common/FormFeldDatePicker";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { VereinAutocomplete } from "@/core/components/verein/VereinAutocomplete";
import type { VerbrauchskostenVorschau } from "@/vermietung/types/VerbrauchskostenVorschau";
import type { Buchung } from "@/vermietung/types/Buchung";
import { BERECHNUNGSART_LABELS } from "@/vermietung/enums/Berechnungsart";
import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";

interface Props {
  expanded: BuchungBereich[];
  onExpandedChange: (bereiche: BuchungBereich[]) => void;
  bereichFehler: Partial<Record<BuchungBereich, string>>;
  abrechnung: boolean;
  verbrauchVeraltet: boolean;
  form: BuchungSave;
  neueBuchung: boolean;
  editMode: boolean;
  mietbereiche: Mietbereich[];
  rabatteByMietbereich: Record<number, MietbereichRabatt[]>;
  mieter?: PersonRef;
  veranstalter?: VereinRef;
  buchungspositionen: Buchung["positionen"];
  rabattSnapshotVerwenden?: boolean;
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

  useEffect(() => { setPreise({}); setLadefehler({}); }, [buchungsquelle, stichtag]);

  const mietbereichIdsKey = [...new Set(mietbereichIds)].sort((a, b) => a - b).join(",");

  useEffect(() => {
    let cancelled = false;

    const ids = mietbereichIdsKey ? mietbereichIdsKey.split(",").map(Number) : [];

    const gespeicherteIds = new Set(buchungspositionen.map((position) => position.mietbereichId));

    const fehlendeIds = ids.filter((id) => !gespeicherteIds.has(id) && preise[id] === undefined && !ladefehler[id]);

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
  }, [mietbereichIdsKey, buchungspositionen, buchungsquelle, stichtag, preise, ladefehler]);

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
  expanded, onExpandedChange, bereichFehler, abrechnung, verbrauchVeraltet,
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
  rabattSnapshotVerwenden = false,
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

  const heute = new Intl.DateTimeFormat("sv-SE", { timeZone: "Europe/Berlin" }).format(new Date());
  const stichtag = form.mietpreiseNeuBerechnen ? form.mietpreisStichtag || heute : createdAt ? createdAt.slice(0, 10) : heute;
  const preispositionen = useMemo(() => form.mietpreiseNeuBerechnen ? [] : buchungspositionen, [form.mietpreiseNeuBerechnen, buchungspositionen]);
  const bisherigerMietpreis = buchungspositionen.reduce((summe, p) => summe + geldRunden((p.vereinbarterPreis ?? 0) * p.anzahl * mietEinheiten(p.berechnungsart, form.anreise, form.abreise, form.unbefristet)), 0);

  const { mietobjekt, loading: mietobjektLoading } = useVermietungContext();

  const { preise: geladenePreise, ladefehler } = useMietbereichPreise(
    form.positionen.map((position) => position.mietbereichId),
    preispositionen,
    buchungsquelle,
    stichtag,
  );

  const einheitenFuer = (mietbereichId: number) => mietEinheiten(
    preispositionen.find((p) => p.mietbereichId === mietbereichId)?.berechnungsart
      ?? mietbereiche.find((p) => p.id === mietbereichId)?.berechnungsart,
    form.anreise, form.abreise, form.unbefristet,
  );

  const kostenlos = form.zahlungsvariante === "OHNE_ABRECHNUNG";
  const gesamtpreis = kostenlos ? 0 : form.positionen.reduce<number>((summe, position) => {
    const buchungsposition = preispositionen.find(
      (item) => item.mietbereichId === position.mietbereichId,
    );

    const preis = buchungsposition?.vereinbarterPreis ?? geladenePreise[position.mietbereichId];

    if (preis === undefined || ladefehler[position.mietbereichId]) {
      return summe;
    }

    return summe + geldRunden(preis * position.anzahl * einheitenFuer(position.mietbereichId));
  }, 0);

  const gesamtpreisVollstaendig = kostenlos || form.positionen.every((position) => {
    const buchungsposition = preispositionen.find(
      (item) => item.mietbereichId === position.mietbereichId,
    );

    return (
      buchungsposition?.vereinbarterPreis !== undefined ||
      (geladenePreise[position.mietbereichId] !== undefined && !ladefehler[position.mietbereichId])
    );
  });

  const rabattbetrag = (() => {
    if (form.buchungsquelle === "AIRBNB" || form.rabattart === Rabattart.KEIN_RABATT) return 0;
    if (form.rabattart === Rabattart.INDIVIDUELL && form.rabattwertTyp === RabattwertTyp.ABSOLUT) {
      return Math.min(gesamtpreis, Math.max(0, form.rabattwert ?? 0));
    }

    return form.positionen.reduce((summe, position) => {
      const areaPrice = preispositionen.find((item) => item.mietbereichId === position.mietbereichId)?.vereinbarterPreis
        ?? geladenePreise[position.mietbereichId];
      if (areaPrice === undefined || ladefehler[position.mietbereichId]) return summe;

      const faktor = position.anzahl * einheitenFuer(position.mietbereichId);
      const positionspreis = geldRunden(areaPrice * faktor);
      const aktiveRabatte = (rabatteByMietbereich[position.mietbereichId] ?? []).filter((rabatt) => rabatt.aktiv);
      let regel: MietbereichRabatt | undefined;
      let wertTyp: RabattwertTyp | null = null;
      let wert: number | null = null;

      const snapshot = rabattSnapshotVerwenden
        ? buchungspositionen.find((p) => p.mietbereichId === position.mietbereichId)
        : undefined;
      if (snapshot) {
        wertTyp = snapshot.rabattwertTyp ?? null;
        wert = snapshot.rabattwert ?? null;
      } else if (form.rabattart === Rabattart.INDIVIDUELL && form.rabattwertTyp === RabattwertTyp.PROZENTUAL) {
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
        return summe + geldRunden(Math.min(positionspreis, positionspreis * wert / 100));
      }
      if (wertTyp === RabattwertTyp.ABSOLUT && wert != null) {
        return summe + geldRunden(Math.min(positionspreis, wert * faktor));
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

  const [verbrauchKonfiguration, setVerbrauchKonfiguration] = useState<Verbrauchskosten[] | null>(null);
  const [verbrauchLadefehler, setVerbrauchLadefehler] = useState(false);
  useEffect(() => {
    let aktiv = true;
    setVerbrauchKonfiguration(null);
    setVerbrauchLadefehler(false);
    if (!form.mietobjektId || kostenlos || form.buchungsquelle === "AIRBNB") {
      setVerbrauchKonfiguration([]);
      return;
    }
    getVerbrauchskosten(form.mietobjektId).then(rows => {
      if (aktiv) setVerbrauchKonfiguration(rows.filter(row => row.aktiv));
    }).catch(() => { if (aktiv) setVerbrauchLadefehler(true); });
    return () => { aktiv = false; };
  }, [form.mietobjektId, kostenlos, form.buchungsquelle]);

  const mieterName = formatPersonName(mieter?.name, mieter?.vorname, "first") || "Mieter noch nicht gewählt";
  const datum = (value?: string) => value ? formatDatumZeit(value) : "Noch nicht erfasst";
  const zeitraum = `${datum(form.anreise)} – ${form.unbefristet ? "ohne Enddatum" : datum(form.abreise)}`;
  const rabattLabel = { KEIN_RABATT: "kein Rabatt", VEREIN: "Vereinsrabatt", VERBAND: "Verbandsrabatt", INDIVIDUELL: "individueller Rabatt" }[form.rabattart];
  const mietpreisLabel = !form.positionen.length ? "noch keine Mietbereiche gewählt" : !form.anreise || (!form.unbefristet && !form.abreise) ? "Zeitraum noch nicht vollständig" : gesamtpreisVollstaendig ? formatPreis(mietpreisNachRabatt) : "noch nicht vollständig ermittelt";
  const schaeden = form.entschaedigungen ?? [];
  const schaedenSumme = schaeden.reduce((sum, row) => sum + Math.round(row.einzelpreis * 100) * row.anzahl / 100, 0);
  const verbrauchRows = buchungsstatus === "ABGESCHLOSSEN" ? verbrauchskosten ?? [] : verbrauchskostenVorschau ?? [];
  const verbrauchSummary = kostenlos ? "Keine Verbrauchspositionen für diese Buchung"
    : form.buchungsquelle === "AIRBNB" ? "Airbnb · Abschluss ohne Verbrauchswerte möglich"
    : verbrauchRows.length ? verbrauchRows.map(k => `${k.bezeichnung}: ${k.verbrauch == null ? k.kosten == null ? "Noch nicht erfasst" : formatPreis(k.kosten) : `${formatZahl(k.verbrauch)} ${k.mengeneinheit ?? ""}`}`).join(" · ")
    : verbrauchKonfiguration?.length === 0 ? "Keine Verbrauchspositionen für diese Buchung" : "Noch nicht erfasst";
  const verbrauchGesamtLabel = verbrauchRows.length && verbrauchRows.every(k => k.kosten != null && (!("erfolgreich" in k) || k.erfolgreich))
    ? ` · Verbrauchskosten ${formatPreis(verbrauchRows.reduce((sum,k) => sum + (k.kosten ?? 0), 0))}` : '';
  const bereich = (id: BuchungBereich, titel: string, summary: string, children: React.ReactNode) => (
    <Accordion key={id} expanded={expanded.includes(id)} onChange={(_, offen) => onExpandedChange(offen ? [...expanded, id] : expanded.filter(b => b !== id))}
      disableGutters sx={{ minWidth: 0, '&:before': { display: 'none' } }}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />} id={`buchung-${id}-kopf`} aria-controls={`buchung-${id}-inhalt`}>
        <Box sx={{ minWidth: 0, overflowWrap: 'anywhere' }}>
          <Typography component="span" fontWeight={600}>{titel}</Typography>
          <Typography component="span" variant="body2" color="text.secondary" sx={{ display: 'block' }}>{summary}</Typography>
          {bereichFehler[id] && <Typography component="span" role="alert" color="error" sx={{ display: 'block' }}>Fehler: {bereichFehler[id]}</Typography>}
        </Box>
      </AccordionSummary>
      <AccordionDetails data-buchung-bereich={id}
        sx={{ ...(id === "mieter" || id === "zahlung" ? {maxWidth: 760} : {}), display: 'grid', gap: 2, minWidth: 0, scrollMarginBottom: '160px' }}>{children}</AccordionDetails>
    </Accordion>
  );

  return <Box sx={{ display: 'grid', gap: 1.5, minWidth: 0 }}>
    <Typography sx={{ overflowWrap: 'anywhere' }}>{mieterName} · {zeitraum} · Mietpreis nach Rabatt {mietpreisLabel}
      {form.fruehereBuchungsnummer && ` · frühere Buchungsnummer ${form.fruehereBuchungsnummer}`}</Typography>
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
      <Button onClick={() => onExpandedChange(['mieter', 'aufenthalt', 'miete', 'zahlung', 'verbrauch', 'schaeden', 'intern'])}>Alle aufklappen</Button>
      <Button onClick={() => onExpandedChange([])}>Alle zuklappen</Button>
    </Box>
    {bereich('mieter', 'Mieter und Veranstalter', `${mieterName} · ${veranstalter?.name || 'kein Veranstalter'} · ${rabattLabel}`, <>
        <PersonAutocomplete
          label="Mieter"
          vermietung
          value={mieter}
          disabled={!buchungsdatenEditierbar}
          onChange={onMieterChange}
        />

        <VereinAutocomplete
          label="Veranstalter"
          veranstalter
          value={veranstalter}
          disabled={!buchungsdatenEditierbar}
          onChange={onVeranstalterChange}
        />
        {kostenlos && <Alert severity="info">Diese Buchung ist vollständig kostenlos. Es werden keine Miet-, Verbrauchs- oder Zusatzkosten berechnet. Nach Mietende wird die bestätigte Buchung automatisch abgeschlossen.</Alert>}
        {veranstalter && !kostenlos && <Alert severity="info">
          Rechnung an: {veranstalter.name}<br />
          Ansprechpartner: {formatPersonName(mieter?.name, mieter?.vorname, "first")}
        </Alert>}


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
          <Box sx={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(min(100%, 160px), 1fr))", gap: 1 }}>
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
    </>)}
    {bereich('aufenthalt', 'Aufenthalt und Gruppe', `${zeitraum} · ${!form.unbefristet && form.anreise && form.abreise ? `${mietEinheiten(Berechnungsart.TAG, form.anreise, form.abreise)} Miettage · ` : ''}${form.anzahlJugendliche == null && form.anzahlErwachsene == null ? 'Teilnehmer nicht erfasst' : `Jugendliche ${form.anzahlJugendliche ?? 'nicht erfasst'} · Erwachsene ${form.anzahlErwachsene ?? 'nicht erfasst'}`}`, <>
        <TextField
          label="Veranstaltung"
          value={form.veranstaltung ?? ""}
          onChange={(event) => onChange("veranstaltung", event.target.value)}
          disabled={!buchungsdatenEditierbar}
          inputProps={{ maxLength: 80 }}
          helperText={`${(form.veranstaltung ?? "").length}/80 Zeichen (optional)`}
          fullWidth
          size="small"
        />

<Box sx={{ display: "grid", gridTemplateColumns: "1fr", gap: 2, "@container buchungsmaske (min-width: 720px)": { gridTemplateColumns: "1fr 1fr" } }}>
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: "1fr", "@container buchungsmaske (min-width: 720px)": { gridTemplateColumns: "minmax(0,2fr) minmax(0,1fr)" },
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
          <FormFeldDatePicker
            label="Beginn"
            referenceDate={!form.unbefristet ? form.abreise?.slice(0, 10) || undefined : undefined}
            value={form.anreise?.slice(0, 10) ?? ""}
            disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.anreisezeit))}
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
            label="Beginn Uhrzeit"
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

        <Box sx={{ display: "grid", gap: 1 }}>
        {!form.unbefristet && (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: "1fr", "@container buchungsmaske (min-width: 720px)": { gridTemplateColumns: "minmax(0,2fr) minmax(0,1fr)" },
              gap: 2,
            }}
          >
            <FormFeldDatePicker
              label="Ende"
              minDate={form.anreise?.slice(0, 10) || undefined}
              referenceDate={form.anreise?.slice(0, 10) || undefined}
              value={form.abreise?.slice(0, 10) ?? ""}
              disabled={!buchungsdatenEditierbar || (neueBuchung && (mietobjektLoading || !mietobjekt?.abreisezeit))}
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
              label="Ende Uhrzeit"
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

        </Box>
        {!form.unbefristet && form.anreise && form.abreise && form.abreise <= form.anreise && (
          <Alert severity="info" sx={{ gridColumn: "1 / -1" }}>
            Die Abreise muss zeitlich nach der Anreise liegen. Für eine Tagesveranstaltung am selben Tag
            stelle bitte die Abreiseuhrzeit später als die Anreiseuhrzeit ein.
          </Alert>
        )}


</Box>
        <Typography variant="h6">Teilnehmer</Typography>
        <Typography variant="body2" color="text.secondary">
          Leere Felder bedeuten „nicht erfasst“. Bei Jugendlichen bitte das ungefähre Alter angeben.
        </Typography>
        <Box sx={{ display: "grid", gridTemplateColumns: "1fr", "@container buchungsmaske (min-width: 720px)": { gridTemplateColumns: "minmax(0, 2fr) minmax(0, 1fr)" }, gap: 2 }}>
          <Box sx={{ display: "grid", gap: 2, gridTemplateColumns: "minmax(0,1fr)", "@container buchungsmaske (min-width: 720px)": { gridTemplateColumns: "repeat(2,minmax(0,1fr))" } }}>
<Typography variant="subtitle2" sx={{gridColumn:"1 / -1"}}>Jugendliche und Altersspanne</Typography>
          <TextField
            sx={{gridColumn:"1 / -1"}}
            name="anzahlJugendliche" label="Anzahl Jugendliche"
            type="number"
            value={form.anzahlJugendliche ?? ""}
            disabled={!buchungsdatenEditierbar}
            required={false}
            slotProps={{ htmlInput: { min: 0, step: 1 } }}
            onChange={(event) => {
              const value = event.target.value === "" ? null : Number(event.target.value);
              onChange("anzahlJugendliche", value);
              if (value == null || value === 0) {
                onChange("jugendAlterVon", null);
                onChange("jugendAlterBis", null);
              }
            }}
            fullWidth
          />
          <TextField
            name="jugendAlterVon" label="Alter ungefähr von"
            type="number"
            value={form.jugendAlterVon ?? ""}
            disabled={!buchungsdatenEditierbar || !(form.anzahlJugendliche && form.anzahlJugendliche > 0)}
            required={!!form.anzahlJugendliche && form.anzahlJugendliche > 0}
            slotProps={{ htmlInput: { min: 0, step: 1, max: 120 } }}
            onChange={(event) => {
              const value = event.target.value === "" ? null : Number(event.target.value);
              onChange("jugendAlterVon", value);
            }}
            fullWidth
          />
          <TextField
            name="jugendAlterBis" label="Alter ungefähr bis"
            type="number"
            value={form.jugendAlterBis ?? ""}
            disabled={!buchungsdatenEditierbar || !(form.anzahlJugendliche && form.anzahlJugendliche > 0)}
            required={!!form.anzahlJugendliche && form.anzahlJugendliche > 0}
            slotProps={{ htmlInput: { min: 0, step: 1, max: 120 } }}
            onChange={(event) => {
              const value = event.target.value === "" ? null : Number(event.target.value);
              onChange("jugendAlterBis", value);
            }}
            fullWidth
          />
          </Box>
          <TextField
            name="anzahlErwachsene" label="Anzahl Erwachsene"
            type="number"
            value={form.anzahlErwachsene ?? ""}
            disabled={!buchungsdatenEditierbar}
            required={false}
            slotProps={{ htmlInput: { min: 0, step: 1 } }}
            onChange={(event) => {
              const value = event.target.value === "" ? null : Number(event.target.value);
              onChange("anzahlErwachsene", value);
            }}
            fullWidth
          />
        </Box>
        {form.anzahlJugendliche != null && form.anzahlErwachsene != null && (
          <Typography>Teilnehmer gesamt: {form.anzahlJugendliche + form.anzahlErwachsene}</Typography>
        )}

    </>)}
    {bereich('miete', 'Mietbereiche und Preise', `${form.positionen.map(p => mietbereiche.find(b => b.id === p.mietbereichId)?.bezeichnung || 'Mietbereich').join(' + ') || 'Keine Mietbereiche gewählt'} · Miete nach Rabatt ${mietpreisLabel}`, <>
      <Box sx={{ minWidth: 0, display: "grid", gap: 2, gridTemplateColumns: "minmax(0,1fr)", "@container buchungsmaske (min-width: 960px)": { gridTemplateColumns: "minmax(0,2fr) minmax(0,1fr)" } }}>
<Box sx={{minWidth:0}}>

          <Typography variant="subtitle1" fontWeight={600} color="text.secondary" sx={{ mb: 1.5 }}>
            Mietbereiche / Positionen
          </Typography>

          {!neueBuchung && buchungsdatenEditierbar && <Box sx={{ mb: 2 }}>
            <FormControlLabel label="Mietpreise aus Katalog neu übernehmen" control={<Checkbox checked={!!form.mietpreiseNeuBerechnen} onChange={e => onChange("mietpreiseNeuBerechnen", e.target.checked)} />} />
            {form.mietpreiseNeuBerechnen && <Box sx={{ display: "grid", gap: 1 }}>
              <TextField label="Preisstand (Stichtag)" type="date" size="small" value={form.mietpreisStichtag ?? heute} error={form.mietpreisStichtag === ""} helperText={form.mietpreisStichtag === "" ? "Bitte einen Preisstand für die Neuberechnung wählen." : "Verwendet wird der am Stichtag gültige Preis des Mietpreiskatalogs."} onChange={e => onChange("mietpreisStichtag", e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
              <Alert severity="warning">Beim Speichern werden Einzelpreis und Berechnungsart aller gewählten Mietpositionen aus dem Katalog zu diesem Stichtag übernommen. Die gewählte Rabattregel bleibt erhalten. Ohne Speichern bleibt der bisherige Mietpreis bestehen.</Alert>
              <Typography variant="body2">Bisherige Preisbasis vor Rabatt: {formatPreis(bisherigerMietpreis)} · Vorschau aus Katalog: {gesamtpreisVollstaendig ? formatPreis(gesamtpreis) : "Preise werden ermittelt …"}</Typography>
              {form.positionen.some(p => ladefehler[p.mietbereichId]) && <Alert severity="error">Für mindestens eine gewählte Position fehlt ein gültiger Katalogpreis am Stichtag. Bitte Preisstand oder Mietpreiskatalog prüfen.</Alert>}
            </Box>}
          </Box>}

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
                        {mietbereich.bestand !== 1 && (
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
                        )}

                        {(() => {
                          const buchungsposition = preispositionen.find(
                            (item) => item.mietbereichId === mietbereich.id,
                          );

                          const preis = kostenlos ? 0 :
                            buchungsposition?.vereinbarterPreis ?? geladenePreise[mietbereich.id];

                          const berechnungsart =
                            buchungsposition?.berechnungsart ?? mietbereich.berechnungsart;

                          return (
                            <Box sx={{ minWidth: 110 }}>
                              <Typography variant="body2" fontWeight={600}>
                                {preis !== undefined
                                  ? formatPreis(geldRunden(preis * position.anzahl * einheitenFuer(mietbereich.id)))
                                  : ladefehler[mietbereich.id]
                                    ? "Preis nicht verfügbar"
                                    : "Preis wird geladen …"}
                              </Typography>

                              {berechnungsart && (
                                <Typography variant="caption" color="text.secondary">
                                  {BERECHNUNGSART_LABELS[berechnungsart]}
                                  {berechnungsart === "TAG" && !form.unbefristet && ` · ${einheitenFuer(mietbereich.id)} Miettage`}
                                  {berechnungsart === "TAG" && form.unbefristet && " · vorläufig ohne Laufzeit"}
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

      </Box>
          <Box sx={{ mb: 2 }}>
            {kostenlos ? <><Typography variant="subtitle2">Ohne Abrechnung</Typography><Typography variant="h5">0,00 € · kostenlos</Typography></> : form.buchungsquelle === "AIRBNB" ? (
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
                {(
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

      </Box>
    </>)}
    {bereich('zahlung', 'Zahlung und Bankverbindung', `${form.buchungsquelle === 'AIRBNB' ? 'Airbnb' : { ANZAHLUNG: 'Anzahlung', BEI_ABRECHNUNG: 'Zahlung bei Abrechnung', GESAMTMIETE_VORAUS: 'Gesamtmiete im Voraus', OHNE_ABRECHNUNG: 'Ohne Abrechnung' }[form.zahlungsvariante ?? 'ANZAHLUNG']} · Kaution ${formatPreis(form.kautionBetrag)} · ${form.bankverbindungAuswahl === 'ALTERNATIV' ? 'Alternativbank' : 'Primärbank'}`, <>
        {form.buchungsquelle === "DIREKT" && <TextField select label="Zahlungsvariante" fullWidth size="small"
          value={form.zahlungsvariante ?? "ANZAHLUNG"} disabled={!buchungsdatenEditierbar}
          helperText="Ohne Abrechnung: vollständig kostenlos, ohne Anzahlung und Kaution; automatischer Abschluss nach Mietende."
          onChange={e => onChange("zahlungsvariante", e.target.value as BuchungSave["zahlungsvariante"])}>
          <MenuItem value="OHNE_ABRECHNUNG">Ohne Abrechnung (kostenlos)</MenuItem>
          <MenuItem value="ANZAHLUNG">Anzahlung</MenuItem>
          <MenuItem value="BEI_ABRECHNUNG">Zahlung bei Abrechnung</MenuItem>
          <MenuItem value="GESAMTMIETE_VORAUS">Gesamtmiete im Voraus</MenuItem>
        </TextField>}
        {form.buchungsquelle === "DIREKT" && (form.zahlungsvariante ?? "ANZAHLUNG") === "ANZAHLUNG" ? (
          <TextField
            label="Anzahlung (€)"
            type="number"
            value={form.anzahlungBetrag ?? 0}
            disabled={!buchungsdatenEditierbar}
            fullWidth
            size="small"
            slotProps={{ htmlInput: { min: 0, step: "0.01" } }}
            onChange={(event) => onChange("anzahlungBetrag", Math.max(0, Number(event.target.value) || 0))}
          />
        ) : null}

        {form.buchungsquelle === "DIREKT" && !kostenlos ? (
          <TextField
            label="Kaution (€)"
            type="number"
            value={form.kautionBetrag ?? 0}
            disabled={!buchungsdatenEditierbar}
            fullWidth
            size="small"
            slotProps={{ htmlInput: { min: 0, step: "0.01" } }}
            onChange={(event) => onChange("kautionBetrag", Math.max(0, Number(event.target.value) || 0))}
          />
        ) : form.buchungsquelle === "AIRBNB" ? (
          <Alert severity="info">Für Airbnb-Buchungen werden keine Anzahlung und keine Kaution erhoben.</Alert>
        ) : null}

        {!kostenlos && buchungsstatus === "ANFRAGE" && form.buchungsquelle === "DIREKT" && form.zahlungsvariante !== "GESAMTMIETE_VORAUS" && form.anreise && (() => {
          const heute = new Intl.DateTimeFormat("sv-SE", { timeZone: "Europe/Berlin" }).format(new Date());
          return (Date.parse(form.anreise.slice(0, 10)) - Date.parse(heute)) / 86400000 < (mietobjekt?.kurzfristgrenzeTage ?? 42) && (mietobjekt?.kurzfristgrenzeTage ?? 42) > 0;
        })() && <Alert severity="info" action={<button type="button" disabled={!buchungsdatenEditierbar} onClick={() => onChange("zahlungsvariante", "GESAMTMIETE_VORAUS")}>Übernehmen</button>}>Kurzfristige Anfrage: Gesamtmiete im Voraus empfohlen.</Alert>}
        <BuchungBankverbindung
          mietobjektId={form.mietobjektId}
          value={form.bankverbindungAuswahl ?? "PRIMAER"}
          disabled={!buchungsdatenEditierbar}
          onChange={(value) => onChange("bankverbindungAuswahl", value)}
        />


    </>)}
    {bereich('verbrauch', 'Verbrauch und Zählerstände', verbrauchVeraltet ? 'Verbrauchsvorschau nach Speichern aktualisieren' : verbrauchSummary + verbrauchGesamtLabel, <>
      {verbrauchVeraltet && <Alert severity="info">Die folgenden Verbrauchswerte gehören zum gespeicherten Zeitraum. Bitte die Buchung speichern, um die Vorschau für den geänderten Zeitraum zu aktualisieren.</Alert>}
      <Box sx={{ minWidth: 0 }}>

          <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
            Verbrauchswerte und Kosten
          </Typography>

          {kostenlos ? <Typography>Keine Verbrauchspositionen für diese Buchung</Typography>
          : form.buchungsquelle !== "AIRBNB" && buchungsstatus !== "ABRECHENBAR" && buchungsstatus !== "ABGESCHLOSSEN" ? (
            <Box sx={{ display: "grid", gap: 2 }}>
              {verbrauchLadefehler ? <Alert severity="error">Verbrauchspositionen konnten nicht geladen werden.</Alert>
                : verbrauchKonfiguration === null ? <Typography>Noch nicht erfasst · Verbrauchspositionen werden geladen …</Typography>
                : verbrauchKonfiguration.length === 0 ? <Typography>Keine Verbrauchspositionen für diese Buchung</Typography>
                : verbrauchKonfiguration.map(k => <Box key={k.id}>
                    <Typography fontWeight={600}>{k.bezeichnung}</Typography>
                    <Typography variant="body2">{k.abrechnungsart === "ZAEHLERSTAND" ? `Zählerstand · ${formatPreis(k.preis)} je ${k.mengeneinheit || "Einheit"}` : k.abrechnungsart === "TAGESPREIS" ? `Tagespreis · ${formatPreis(k.preis)}` : `Pauschale · ${formatPreis(k.preis)}`}</Typography>
                    <Typography variant="body2" color="text.secondary">Noch nicht erfasst</Typography>
                  </Box>)}
            </Box>
          ) : form.buchungsquelle === "AIRBNB" ? (
            <Typography variant="body2" color="text.secondary">
              Für Airbnb-Buchungen ist keine Zählerstandsprüfung beim Abschluss erforderlich.
            </Typography>
          ) : buchungsstatus === "ABRECHENBAR" ? (
            !verbrauchskostenVorschau || verbrauchskostenVorschau.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                Keine Verbrauchspositionen für diese Buchung
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
                        color={kosten.bestaetigungErforderlich ? "warning.main" : "error"}
                        sx={{ mt: 1, overflowWrap: "anywhere" }}
                      >
                        {kosten.hinweis ?? "Die Verbrauchskosten konnten nicht berechnet werden."}
                      </Typography>
                    )}

                    {(kosten.erfolgreich || kosten.bestaetigungErforderlich) && (
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
              Liegt eine nächstliegende Ablesung mehr als 24 Stunden vom Termin entfernt,
              muss ihre Verwendung beim Abschluss bestätigt werden.
            </Typography>
          ) : !verbrauchskosten || verbrauchskosten.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Keine Verbrauchspositionen für diese Buchung
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

      </Box>

      {abrechnung && <Typography variant="body2" color="text.secondary">Ablesungen werden im bestehenden Abschlussdialog geprüft und gegebenenfalls ausdrücklich bestätigt. Das Öffnen dieses Bereichs ändert keine Zählerstände.</Typography>}
    </>)}
    {bereich('schaeden', 'Schäden und Fehlbestände', schaeden.length ? `${schaeden.length} Positionen · ${formatPreis(schaedenSumme)}` : 'Keine Schäden erfasst', kostenlos ? <Typography>Diese Buchung ist vollständig kostenlos.</Typography> :
      <EntschaedigungEditor objektId={form.mietobjektId} rows={schaeden} editable={editMode} onChange={rows => onChange('entschaedigungen', rows)} />)}
    {bereich('intern', 'Interne Angaben', [form.bemerkung ? 'Bemerkung vorhanden' : 'Keine Bemerkung', form.bewertung != null ? `Bewertung ${form.bewertung}/5` : '', form.fruehereBuchungsnummer ? `frühere Nr. ${form.fruehereBuchungsnummer}` : ''].filter(Boolean).join(' · '), <>
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

        <TextField label="Frühere Buchungsnummer" value={form.fruehereBuchungsnummer ?? ""}
          onChange={event => onChange("fruehereBuchungsnummer", event.target.value)}
          disabled={!editMode} inputProps={{maxLength:100}} fullWidth size="small"
          helperText="Optional für die Migration; die aktuelle Buchungsnummer bleibt erhalten." />
    </>)}
  </Box>;
};
