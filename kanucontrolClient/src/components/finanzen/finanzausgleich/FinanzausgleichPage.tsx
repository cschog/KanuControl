import { useCallback, useEffect, useState } from "react";

import { Alert, Box, Typography } from "@mui/material";

import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

import {
  getFinanzausgleich,
  getFinanzgruppen,
  pruefeTeilnehmerBeitraege,
} from "@/api/services/finanzgruppenApi";

import { FinanzausgleichPruefungDTO } from "@/api/services/finanzgruppenApi";

import FinanzausgleichAccordion from "./FinanzausgleichAccordion";

import { FINANZAUSGLEICH_GRID_TEMPLATE } from "./finanzausgleichLayout";
import { FinanzausgleichRow } from "./finanzausgleichTypes";

interface Props {
  veranstaltungId: number;
}

export default function FinanzausgleichPage({ veranstaltungId }: Props) {
  const [rows, setRows] = useState<FinanzausgleichRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [beitragspruefung, setBeitragspruefung] = useState<FinanzausgleichPruefungDTO | null>(null);

  const loadFinanzausgleich = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      /* =========================
         FINANZGRUPPEN LADEN
         ========================= */

      const groups = await getFinanzgruppen(veranstaltungId);

      /* =========================
        BEITRAGSPRÜFUNG
        ========================= */

     const pruefung = await pruefeTeilnehmerBeitraege(veranstaltungId);

     setBeitragspruefung(pruefung);

      /* =========================
         FINANZAUSGLEICH
         JE GRUPPE LADEN
         ========================= */

      const result = await Promise.all(
        groups.map((gruppe) => getFinanzausgleich(veranstaltungId, gruppe.id)),
      );

      /* =========================
         DTO → UI-ROWS
         ========================= */

    const mappedRows: FinanzausgleichRow[] = result.map((item) => ({
      id: item.finanzGruppeId,

      finanzGruppeId: item.finanzGruppeId,

      kuerzel: item.finanzGruppeKuerzel,

      teilnehmerBeitraegeSoll: Number(item.teilnehmerBeitraegeSoll),

      teilnehmerBeitraegeUeberweisung: Number(item.teilnehmerBeitraegeUeberweisung),

      teilnehmerBeitraegeQuittung: Number(item.teilnehmerBeitraegeQuittung),

      ausgaben: Number(item.ausgaben),

      fahrkosten: Number(item.fahrkosten),

      erstattungVomVK: Number(item.erstattungVomVK),

      beitragsstatus: pruefung.finanzgruppen.find(
        (gruppe) => gruppe.finanzGruppeId === item.finanzGruppeId,
      )?.beitragsstatus,
    }));

      setRows(mappedRows);
    } catch (err: unknown) {
      console.error("Fehler beim Laden des Finanzausgleichs", err);

        setError(getApiErrorMessage(err));
        setBeitragspruefung(null);

      setRows([]);
    } finally {
      setLoading(false);
    }
  }, [veranstaltungId]);

  useEffect(() => {
    loadFinanzausgleich();
  }, [loadFinanzausgleich]);

  return (
    <Box
      sx={{
        minHeight: {
          md: "calc(100vh - 140px)",
        },

        display: "flex",
        flexDirection: "column",
      }}
    >
      <Typography
        variant="h4"
        sx={{
          mb: {
            xs: 0.5,
            md: 1,
          },

          fontSize: {
            xs: "1.5rem",
            sm: "1.75rem",
            md: "2.125rem",
          },
        }}
      >
        Finanzausgleich
      </Typography>

      <Typography
        variant="h6"
        color="text.secondary"
        sx={{
          mb: {
            xs: 2,
            md: 3,
          },

          fontSize: {
            xs: "0.95rem",
            sm: "1.05rem",
            md: "1.25rem",
          },

          lineHeight: {
            xs: 1.4,
            md: 1.5,
          },
        }}
      >
        Übersicht über die Teilnehmerbeiträge, Ausgaben und Erstattungen der Konten.
      </Typography>

      {beitragspruefung && (
        <Alert
          severity={beitragspruefung.status === "OK" ? "success" : "error"}
          sx={{
            mb: {
              xs: 2,
              md: 3,
            },
          }}
        >
          <Typography fontWeight={700}>
            {beitragspruefung.status === "OK"
              ? "Teilnehmerbeiträge stimmen"
              : beitragspruefung.status === "FINANZGRUPPEN_ABWEICHUNG"
                ? "Teilnehmer sind möglicherweise dem falschen Konto zugeordnet"
                : beitragspruefung.status === "BEITRAEGE_FEHLEN"
                  ? "Teilnehmerbeiträge fehlen"
                  : "Teilnehmerbeiträge sind zu hoch"}
          </Typography>

          <Typography variant="body2">
            {beitragspruefung.status === "FINANZGRUPPEN_ABWEICHUNG"
              ? "Bitte prüfen Sie, ob alle Teilnehmer dem richtigen Konto zugeordnet sind und niemand in einem Konto fehlt."
              : `Soll: ${beitragspruefung.gesamtSoll.toFixed(2)} € · Ist: ${beitragspruefung.gesamtIst.toFixed(2)} €`}
          </Typography>
        </Alert>
      )}

      {rows.length === 0 && !loading ? (
        <Alert severity="info">Es sind noch keine Finanzgruppen vorhanden.</Alert>
      ) : (
        <Box
          sx={{
            flex: 1,
            minHeight: 0,
          }}
        >
          <Box
            sx={{
              border: 1,
              borderColor: "divider",
              borderRadius: 1,
              overflow: "hidden",
            }}
          >
            {/* =========================
                KOPFZEILE – DESKTOP
               ========================= */}

            <Box
              sx={{
                display: {
                  xs: "none",
                  md: "grid",
                },

                width: "100%",
                minWidth: 0,

                gridTemplateColumns: FINANZAUSGLEICH_GRID_TEMPLATE,

                px: 4,
                py: 1,

                borderBottom: 1,
                borderColor: "divider",

                alignItems: "center",
              }}
            >
              <Typography fontWeight={700}>Konto</Typography>

              <Typography fontWeight={700} textAlign="right">
                Soll-Beiträge
              </Typography>

              <Typography fontWeight={700} textAlign="right">
                Überweisung
              </Typography>

              <Typography fontWeight={700} textAlign="right">
                Quittung
              </Typography>

              <Typography fontWeight={700} textAlign="right">
                Ausgaben / Einnahmen
              </Typography>

              <Typography fontWeight={700} textAlign="right">
                Fahrkosten
              </Typography>

              {/* Abstand */}

              <Box />

              <Typography fontWeight={700} textAlign="right">
                Finanzausgleich
              </Typography>

              {/* Accordion-Pfeil */}

              <Box />
            </Box>

            {/* =========================
                KONTEN
               ========================= */}

            {rows.map((row) => (
              <FinanzausgleichAccordion
                key={row.id}
                veranstaltungId={veranstaltungId}
                row={row}
                expanded={expandedId === row.id}
                onChange={(isExpanded) => {
                  setExpandedId(isExpanded ? row.id : null);
                }}
              />
            ))}
          </Box>
        </Box>
      )}

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
}
