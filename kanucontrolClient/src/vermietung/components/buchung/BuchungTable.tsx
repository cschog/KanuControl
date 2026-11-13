import { useMemo } from "react";
import { formatPersonName } from "@/core/utils/personName";
import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";

import type { Buchung } from "@/vermietung/types/Buchung";

import { buchungColumnsTanstack } from "@/vermietung/components/buchung/buchungColumnsTanstack";

interface Props {
  data: Buchung[];
  showMietobjekt: boolean;

  selectedId: number | null;

  onSelect: (row: Buchung | null) => void;

  sorting: {
    id: string;
    desc: boolean;
  }[];

  onSortingChange: (
    sorting: {
      id: string;
      desc: boolean;
    }[],
  ) => void;
}

export function BuchungTable({ data, showMietobjekt, selectedId, onSelect, sorting, onSortingChange }: Props) {
  const columns = useMemo(() => buchungColumnsTanstack(showMietobjekt), [showMietobjekt]);
  const visibleSorting = useMemo(() => sorting.filter(({ id }) =>
    id !== (showMietobjekt ? "veranstalter" : "mietobjektBezeichnung")), [sorting, showMietobjekt]);
  return (
    <GenericTableTanstack
      data={data}
      columns={columns}
      selectedRowId={selectedId}
      onSelectRow={(row) => onSelect(row ?? null)}
      manualSorting={false}
      sorting={visibleSorting}
      onSortingChange={onSortingChange}
      getRowBackgroundColor={(row) => (row.buchungsquelle === "AIRBNB" ? "#FFF3E0" : undefined)}
      mobileRenderRow={(row) => (
        <div>
          <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: "1rem" }}>
            <strong style={{ flexShrink: 0 }}>{row.buchungsnummer}</strong>
            <span style={{ minWidth: 0, textAlign: "right", overflowWrap: "anywhere" }}>
              {formatPersonName(row.mieterName, row.mieterVorname, "first")}
            </span>
          </div>

          <div>{showMietobjekt ? row.mietobjektBezeichnung : `Veranstalter: ${row.veranstalter?.name ?? "—"}`}</div>

          <div>
            {formatDate(row.anreise)} – {formatDate(row.abreise)}
          </div>

          <div>{formatStatus(row.status)}</div>
        </div>
      )}
    />
  );
}

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString("de-DE");
}

function formatStatus(status: Buchung["status"]): string {
  switch (status) {
    case "ANFRAGE":
      return "Anfrage";

    case "BESTAETIGT":
      return "Bestätigt";

    case "STORNIERT":
      return "Storniert";

    case "ABGESCHLOSSEN":
      return "Abgeschlossen";

    default:
      return status;
  }
}
