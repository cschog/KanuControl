import { useEffect, useMemo, useState } from "react";
import { ColumnDef } from "@tanstack/react-table";

import { fontSize } from "@/theme/ui";

import { Box, Typography } from "@mui/material";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { getBelegeByFinanzGruppe } from "@/api/services/abrechnungApi";
import { belegColumns } from "@/components/finanzen/abrechnung/belegColumns";

import Money from "@/components/common/Money";
import { getZahlungenByFinanzGruppe } from "@/api/services/zahlungsnachweisApi";
import { FinanzGruppeZahlungDTO } from "@/api/types/beitraege";
import { getReisekostenByFinanzGruppe } from "@/api/services/reisekostenApi";
import { ReisekostenabrechnungListResponse } from "@/api/types/Reisekostenabrechnung";

interface Props {
  veranstaltungId: number;
  finanzGruppeId: number;
}

interface SubsectionTitleProps {
  children: React.ReactNode;
}

function SubsectionTitle({ children }: SubsectionTitleProps) {
  return (
    <Typography
      variant="subtitle1"
      sx={{
        mt: 3,
        mb: 1,
        fontSize: fontSize.subsectionTitle,
        fontWeight: 700,
        color: "text.primary",
        borderBottom: 1,
        borderColor: "divider",
        pb: 0.5,
      }}
    >
      {children}
    </Typography>
  );
}

export default function FinanzGruppeBelege({ veranstaltungId, finanzGruppeId }: Props) {
  const [belege, setBelege] = useState<AbrechnungBeleg[]>([]);
  const [zahlungen, setZahlungen] = useState<(FinanzGruppeZahlungDTO & { id: number })[]>([]);
  const [reisekosten, setReisekosten] = useState<ReisekostenabrechnungListResponse[]>([]);

  const [loadingBelege, setLoadingBelege] = useState(false);
  const [loadingZahlungen, setLoadingZahlungen] = useState(false);
  const [loadingReisekosten, setLoadingReisekosten] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* =========================================================
     ZAHLUNGEN
     ========================================================= */

  const zahlungsColumns = useMemo<ColumnDef<FinanzGruppeZahlungDTO & { id: number }>[]>(
    () => [
      {
        accessorKey: "datum",
        header: "Datum",
        size: 120,
        cell: ({ row }) =>
          row.original.datum ? new Date(row.original.datum).toLocaleDateString("de-DE") : "",
      },
      {
        accessorKey: "zahlungsweg",
        header: "Zahlungsweg",
        size: 150,
      },
      {
        accessorKey: "bemerkung",
        header: "Bemerkung",
        size: 300,
      },
      {
        accessorKey: "betrag",
        header: "Betrag",
        size: 140,
        meta: {
          align: "right",
        },
        cell: ({ row }) => <Money value={row.original.betrag} />,
      },
    ],
    [],
  );

  /* =========================================================
     FAHRKOSTEN
     ========================================================= */

  const reisekostenColumns = useMemo<ColumnDef<ReisekostenabrechnungListResponse>[]>(
    () => [
      {
        accessorKey: "fahrerName",
        header: "Fahrer",
        size: 250,
      },
      {
        accessorKey: "gesamtKilometer",
        header: "Kilometer",
        size: 120,
        meta: {
          align: "right",
        },
      },
      {
        accessorKey: "gesamtBetrag",
        header: "Betrag",
        size: 140,
        meta: {
          align: "right",
        },
        cell: ({ row }) => <Money value={row.original.gesamtBetrag} />,
      },
    ],
    [],
  );

  /* =========================================================
     LOAD
     ========================================================= */

  useEffect(() => {
    let cancelled = false;

    setError(null);

    async function loadBelege() {
      setLoadingBelege(true);

      try {
        const data = await getBelegeByFinanzGruppe(veranstaltungId, finanzGruppeId);

        const sichtbareBelege = data.filter((beleg) => beleg.herkunft !== "TEILNEHMERBEITRAG");

        if (!cancelled) {
          setBelege(sichtbareBelege);
        }
      } catch (err: unknown) {
        console.error("Fehler beim Laden der Belege", err);

        if (!cancelled) {
          setError(`Belege: ${getApiErrorMessage(err)}`);
          setBelege([]);
        }
      } finally {
        if (!cancelled) {
          setLoadingBelege(false);
        }
      }
    }

    async function loadZahlungen() {
      setLoadingZahlungen(true);

      try {
        const data = await getZahlungenByFinanzGruppe(veranstaltungId, finanzGruppeId);

        if (!cancelled) {
          setZahlungen(
            data.map((zahlung) => ({
              ...zahlung,
              id: zahlung.zahlungsnachweisId,
            })),
          );
        }
      } catch (err: unknown) {
        console.error("Fehler beim Laden der Zahlungen", err);

        if (!cancelled) {
          setError(`Zahlungen: ${getApiErrorMessage(err)}`);
          setZahlungen([]);
        }
      } finally {
        if (!cancelled) {
          setLoadingZahlungen(false);
        }
      }
    }

    async function loadReisekosten() {
      setLoadingReisekosten(true);

      try {
        const data = await getReisekostenByFinanzGruppe(veranstaltungId, finanzGruppeId);

        if (!cancelled) {
          setReisekosten(data);
        }
      } catch (err: unknown) {
        console.error("Fehler beim Laden der Fahrkosten", err);

        if (!cancelled) {
          setError(`Fahrkosten: ${getApiErrorMessage(err)}`);
          setReisekosten([]);
        }
      } finally {
        if (!cancelled) {
          setLoadingReisekosten(false);
        }
      }
    }

    loadBelege();
    loadZahlungen();
    loadReisekosten();

    return () => {
      cancelled = true;
    };
  }, [veranstaltungId, finanzGruppeId]);

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box>
      <SubsectionTitle>Einnahmen</SubsectionTitle>

      <GenericTableTanstack
        data={zahlungen}
        columns={zahlungsColumns}
        loading={loadingZahlungen}
        height={200}
        fixedColumnWidths={false}
      />

      <SubsectionTitle>Rechnungen</SubsectionTitle>

      <GenericTableTanstack
        data={belege}
        columns={belegColumns}
        loading={loadingBelege}
        height={250}
        fixedColumnWidths={false}
      />

      {reisekosten.length > 0 && (
        <>
          <SubsectionTitle>Fahrkosten</SubsectionTitle>

          <GenericTableTanstack
            data={reisekosten}
            columns={reisekostenColumns}
            loading={loadingReisekosten}
            height={200}
            fixedColumnWidths={false}
          />
        </>
      )}
      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
}
