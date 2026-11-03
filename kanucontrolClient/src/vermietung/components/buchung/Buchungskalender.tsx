import { getSperrzeiten, type Sperrzeit } from "@/vermietung/api/sperrzeitApi";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  ButtonBase,
  Chip,
  Paper,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import TodayIcon from "@mui/icons-material/Today";
import dayjs, { type Dayjs } from "dayjs";
import "dayjs/locale/de";

import type { Buchung } from "@/vermietung/types/Buchung";

interface Props {
  objekte: { id: number; bezeichnung: string }[];
  buchungen: Buchung[];
  onSelect: (buchung: Buchung) => void;
  canCreateRange: boolean;
  onCreateRange: (anreiseDatum: string, abreiseDatum: string) => void;
}

const wochentage = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"];

const statusLabel: Record<Buchung["status"], string> = {
  ANFRAGE: "Anfrage",
  RESERVIERT: "Reserviert",
  BESTAETIGT: "Bestätigt",
  ABRECHENBAR: "Abrechenbar",
  ABGESCHLOSSEN: "Abgeschlossen",
  ABGELEHNT: "Abgelehnt",
  STORNIERT: "Storniert",
};

const statusFarbe: Record<Buchung["status"], "warning" | "success" | "info" | "error" | "default"> = {
  ANFRAGE: "warning",
  RESERVIERT: "warning",
  BESTAETIGT: "success",
  ABRECHENBAR: "info",
  ABGESCHLOSSEN: "default",
  ABGELEHNT: "error",
  STORNIERT: "error",
};

const mietobjektMarkierungen = ["#1565c0", "#6a1b9a", "#00838f", "#ef6c00", "#2e7d32", "#ad1457"];
const mietobjektFarbe = (mietobjektId: number) =>
  mietobjektMarkierungen[Math.abs(mietobjektId) % mietobjektMarkierungen.length];

const formatUhrzeit = (datum: string) => dayjs(datum).format("HH:mm");

function BuchungChip({ buchung, onSelect, fullWidth = false }: {
  buchung: Buchung;
  onSelect: (buchung: Buchung) => void;
  fullWidth?: boolean;
}) {
  const mietername = `${buchung.mieterVorname} ${buchung.mieterName}`.trim();
  const titel = [
    mietername,
    `Buchung ${buchung.buchungsnummer}`,
    `Status: ${statusLabel[buchung.status]}`,
    buchung.mietobjektBezeichnung,
    `${formatUhrzeit(buchung.anreise)}–${buchung.unbefristet ? "unbefristet" : formatUhrzeit(buchung.abreise)}`,
  ].filter(Boolean).join(" · ");
  const mietobjektfarbe = mietobjektFarbe(buchung.mietobjektId);

  return (
    <Tooltip title={titel} arrow>
      <Chip
        size="small"
        color={statusFarbe[buchung.status]}
        variant={buchung.status === "ABGELEHNT" || buchung.status === "STORNIERT" ? "outlined" : "filled"}
        icon={(
          <Box
            aria-hidden="true"
            sx={{
              width: 9,
              height: 18,
              borderRadius: 0.5,
              border: "1px solid",
              borderColor: mietobjektfarbe,
              background: `repeating-linear-gradient(135deg, ${mietobjektfarbe} 0 3px, transparent 3px 6px)`,
              flexShrink: 0,
            }}
          />
        )}
        label={mietername || `Buchung ${buchung.buchungsnummer}`}
        onClick={(event) => {
          event.stopPropagation();
          onSelect(buchung);
        }}
        sx={{
          justifyContent: "flex-start",
          maxWidth: "100%",
          width: fullWidth ? "100%" : undefined,
          cursor: "pointer",
          "& .MuiChip-label": { overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" },
        }}
      />
    </Tooltip>
  );
}

export default function Buchungskalender({ objekte, buchungen, onSelect, canCreateRange, onCreateRange }: Props) {
  const [sperren, setSperren] = useState<(Sperrzeit & { objektId: number })[]>([]);
  const [sperrfehler, setSperrfehler] = useState<string | null>(null);
  const objektKey = objekte.map(o => o.id).sort((a, b) => a - b).join(",");
  useEffect(() => {
    let cancelled = false;
    setSperren([]); setSperrfehler(null);
    const ids = objektKey ? objektKey.split(",").map(Number) : [];
    Promise.all(ids.map(async id => (await getSperrzeiten(id)).map(s => ({ ...s, objektId: id }))))
      .then(data => { if (!cancelled) setSperren(data.flat()); })
      .catch((err: unknown) => { if (!cancelled) setSperrfehler(getApiErrorMessage(err, "Sperrzeiten konnten nicht geladen werden.")); });
    return () => { cancelled = true; };
  }, [objektKey]);
  const sperrChips = (tag: Dayjs) => sperren.filter(s =>
    dayjs(s.von).isBefore(tag.add(1, "day").startOf("day")) && dayjs(s.bis).isAfter(tag.startOf("day"))
  ).map(s => <Tooltip key={s.id} title={`${objekte.find(o => o.id === s.objektId)?.bezeichnung ?? "Mietobjekt"}: ${s.grund} · ${dayjs(s.von).format("DD.MM. HH:mm")}–${dayjs(s.bis).format("DD.MM. HH:mm")}`}>
    <Chip size="small" label="Gesperrt" sx={{ bgcolor: "grey.700", color: "common.white" }} />
  </Tooltip>);
  const [monat, setMonat] = useState<Dayjs>(() => dayjs().locale("de").startOf("month"));
  const [zeitraum, setZeitraum] = useState<{ start: Dayjs; ende: Dayjs | null } | null>(null);

  const waehleTag = (tag: Dayjs) => {
    const ausgewaehlterTag = tag.startOf("day");
    if (!zeitraum || zeitraum.ende) {
      setZeitraum({ start: ausgewaehlterTag, ende: null });
      return;
    }

    if (ausgewaehlterTag.isBefore(zeitraum.start, "day")) {
      setZeitraum({ start: ausgewaehlterTag, ende: zeitraum.start });
    } else {
      setZeitraum({ ...zeitraum, ende: ausgewaehlterTag });
    }
  };

  const rangeIncludesDay = (tag: Dayjs) => Boolean(
    zeitraum && (
      tag.isSame(zeitraum.start, "day")
      || zeitraum.ende?.isSame(tag, "day")
      || (zeitraum.ende && tag.isAfter(zeitraum.start, "day") && tag.isBefore(zeitraum.ende, "day"))
    ),
  );

  const tage = useMemo(() => {
    const monatsanfang = monat.startOf("month");
    const versatz = (monatsanfang.day() + 6) % 7;
    const kalenderanfang = monatsanfang.subtract(versatz, "day");
    return Array.from({ length: 42 }, (_, index) => kalenderanfang.add(index, "day"));
  }, [monat]);

  const mobileTage = useMemo(
    () => Array.from({ length: monat.daysInMonth() }, (_, index) => monat.date(index + 1)),
    [monat],
  );

  const buchungenNachTag = useMemo(() => {
    const ergebnis = new Map<string, Buchung[]>();

    for (const tag of tage) {
      const tagBuchungen = buchungen.filter((buchung) => {
        const anreise = dayjs(buchung.anreise);
        const abreise = dayjs(buchung.abreise);
        return (
          !anreise.isAfter(tag.endOf("day")) &&
          (buchung.unbefristet || !abreise.isBefore(tag.startOf("day")))
        );
      });

      tagBuchungen.sort((a, b) => a.anreise.localeCompare(b.anreise));
      ergebnis.set(tag.format("YYYY-MM-DD"), tagBuchungen);
    }

    return ergebnis;
  }, [buchungen, tage]);

  const mietobjekte = Array.from(
    new Map(buchungen.map((buchung) => [buchung.mietobjektId, buchung.mietobjektBezeichnung])).entries(),
  );

  return (
    <Box>
      {sperrfehler && <Alert severity="error" sx={{ mb: 2 }}>{sperrfehler}</Alert>}
      <Box
        sx={{
          display: "flex",
          flexDirection: { xs: "column", sm: "row" },
          alignItems: { xs: "stretch", sm: "center" },
          justifyContent: "space-between",
          gap: 2,
          flexWrap: "wrap",
          mb: 2,
        }}
      >
        <Typography variant="h6" textTransform="capitalize" fontWeight={600}>
          {monat.format("MMMM YYYY")}
        </Typography>

        <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
          <Button
            size="small"
            variant="contained"
            fullWidth
            disabled={!zeitraum?.ende || !canCreateRange}
            onClick={() => {
              if (zeitraum?.ende) {
                onCreateRange(zeitraum.start.format("YYYY-MM-DD"), zeitraum.ende.format("YYYY-MM-DD"));
              }
            }}
          >
            <Box component="span" sx={{ display: { xs: "none", sm: "inline" } }}>Buchung für Zeitraum anlegen</Box>
            <Box component="span" sx={{ display: { xs: "inline", sm: "none" } }}>Buchung anlegen</Box>
          </Button>
          <Stack direction="row" spacing={1} justifyContent={{ xs: "space-between", sm: "flex-start" }}>
            <Button
              size="small"
              variant="outlined"
              startIcon={<TodayIcon />}
              onClick={() => setMonat(dayjs().locale("de").startOf("month"))}
            >
              Heute
            </Button>
            <Button
              size="small"
              variant="outlined"
              aria-label="Vorheriger Monat"
              onClick={() => setMonat((aktuell) => aktuell.subtract(1, "month"))}
            >
              <ChevronLeftIcon />
            </Button>
            <Button
              size="small"
              variant="outlined"
              aria-label="Nächster Monat"
              onClick={() => setMonat((aktuell) => aktuell.add(1, "month"))}
            >
              <ChevronRightIcon />
            </Button>
          </Stack>
        </Stack>
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
        {!zeitraum
          ? "Markiere den Beginn und das Ende durch zwei Klicks auf Kalendertage."
          : !zeitraum.ende
            ? `Beginn: ${zeitraum.start.format("DD.MM.YYYY")} – wähle jetzt den Endtag.`
            : `Ausgewählter Zeitraum: ${zeitraum.start.format("DD.MM.YYYY")} bis ${zeitraum.ende.format("DD.MM.YYYY")}.`}
        {!canCreateRange && " Für das Anlegen muss das aktive Mietobjekt im Filter ausgewählt sein."}
      </Typography>

      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mb: 1.5 }}>
        {(Object.keys(statusLabel) as Buchung["status"][]).map((status) => (
          <Chip key={status} size="small" color={statusFarbe[status]} label={statusLabel[status]} />
        ))}
        <Chip size="small" label="Gesperrt" sx={{ bgcolor: "grey.700", color: "common.white" }} />
      </Box>

      {mietobjekte.length > 0 && (
        <Box sx={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 1.5, mb: 1.5 }}>
          <Typography variant="caption" color="text.secondary">Mietobjekte:</Typography>
          {mietobjekte.map(([id, bezeichnung]) => (
            <Box key={id} sx={{ display: "inline-flex", alignItems: "center", gap: 0.5 }}>
              <Box
                aria-hidden="true"
                sx={{
                  width: 14,
                  height: 14,
                  border: "1px solid",
                  borderColor: mietobjektFarbe(id),
                  background: `repeating-linear-gradient(135deg, ${mietobjektFarbe(id)} 0 3px, transparent 3px 6px)`,
                }}
              />
              <Typography variant="caption" color="text.secondary">{bezeichnung}</Typography>
            </Box>
          ))}
        </Box>
      )}

      <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
        Reservierte und bestätigte Buchungen belegen ihre ausgewählten Mietbereiche. Anfragen sind sichtbar, blockieren die Buchbarkeit aber nicht.
      </Typography>

      <Stack spacing={1} sx={{ display: { xs: "flex", sm: "none" } }}>
        {mobileTage.map((tag) => {
          const tagBuchungen = buchungenNachTag.get(tag.format("YYYY-MM-DD")) ?? [];
          const ausgewaehlt = rangeIncludesDay(tag);
          const heute = tag.isSame(dayjs(), "day");

          return (
            <Paper
              key={tag.format("YYYY-MM-DD")}
              variant="outlined"
              role="button"
              tabIndex={0}
              onClick={() => waehleTag(tag)}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  waehleTag(tag);
                }
              }}
              sx={{
                display: "flex",
                alignItems: "flex-start",
                gap: 1.5,
                p: 1,
                minHeight: 56,
                bgcolor: ausgewaehlt ? "action.selected" : "background.paper",
                borderColor: heute || ausgewaehlt ? "primary.main" : "divider",
                borderWidth: heute ? 2 : 1,
                cursor: "pointer",
              }}
            >
              <ButtonBase
                onClick={(event) => {
                  event.stopPropagation();
                  waehleTag(tag);
                }}
                sx={{
                  flex: "0 0 76px",
                  minHeight: 40,
                  borderRadius: 1,
                  justifyContent: "flex-start",
                  textAlign: "left",
                  fontWeight: heute ? 700 : 500,
                  color: heute ? "primary.main" : "text.primary",
                }}
              >
                <Typography variant="body2" fontWeight={heute ? 700 : 500}>
                  {tag.format("dd, D. MMM")}
                </Typography>
              </ButtonBase>
              <Stack spacing={0.5} sx={{ flex: 1, minWidth: 0 }}>
                {sperrChips(tag)}
                {tagBuchungen.length ? tagBuchungen.map((buchung) => (
                  <BuchungChip key={buchung.id} buchung={buchung} onSelect={onSelect} fullWidth />
                )) : (
                  sperrChips(tag).length === 0 && <Typography variant="caption" color="text.disabled" sx={{ py: 1 }}>
                    Keine Buchungen
                  </Typography>
                )}
              </Stack>
            </Paper>
          );
        })}
      </Stack>

      <Box sx={{ display: { xs: "none", sm: "block" }, overflowX: "auto" }}>
        <Box sx={{ minWidth: 840 }}>
          <Box sx={{ display: "grid", gridTemplateColumns: "repeat(7, minmax(0, 1fr))", gap: 0.5, mb: 0.5 }}>
            {wochentage.map((tag) => (
              <Typography key={tag} variant="subtitle2" color="text.secondary" textAlign="center" sx={{ py: 1 }}>
                {tag}
              </Typography>
            ))}
          </Box>

          <Box sx={{ display: "grid", gridTemplateColumns: "repeat(7, minmax(0, 1fr))", gap: 0.5 }}>
            {tage.map((tag) => {
              const tagBuchungen = buchungenNachTag.get(tag.format("YYYY-MM-DD")) ?? [];
              const imMonat = tag.month() === monat.month();
              const heute = tag.isSame(dayjs(), "day");

              return (
                <Paper
                  key={tag.format("YYYY-MM-DD")}
                  variant="outlined"
                  role="button"
                  tabIndex={0}
                  onClick={() => waehleTag(tag)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" || event.key === " ") {
                      event.preventDefault();
                      waehleTag(tag);
                    }
                  }}
                  sx={{
                    minHeight: 124,
                    p: 0.75,
                    bgcolor: rangeIncludesDay(tag) ? "action.selected" : imMonat ? "background.paper" : "action.hover",
                    borderColor: heute || rangeIncludesDay(tag) ? "primary.main" : "divider",
                    borderWidth: heute ? 2 : 1,
                    cursor: "pointer",
                    "&:hover": { borderColor: "primary.main" },
                  }}
                >
                  <ButtonBase
                    onClick={(event) => {
                      event.stopPropagation();
                      waehleTag(tag);
                    }}
                    sx={{
                      display: "block",
                      textAlign: "left",
                      borderRadius: 1,
                      mb: 0.5,
                      px: 0.25,
                      width: "100%",
                    }}
                  >
                    <Typography
                      variant="body2"
                      fontWeight={heute ? 700 : 500}
                      color={imMonat ? "text.primary" : "text.disabled"}
                    >
                      {tag.date()}
                    </Typography>
                  </ButtonBase>

                  <Stack spacing={0.5}>
                    {sperrChips(tag)}
                    {tagBuchungen.slice(0, 4).map((buchung) => (
                      <BuchungChip key={buchung.id} buchung={buchung} onSelect={onSelect} />
                    ))}
                    {tagBuchungen.length > 4 && (
                      <Typography variant="caption" color="text.secondary" sx={{ px: 0.5 }}>
                        +{tagBuchungen.length - 4} weitere
                      </Typography>
                    )}
                  </Stack>
                </Paper>
              );
            })}
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
