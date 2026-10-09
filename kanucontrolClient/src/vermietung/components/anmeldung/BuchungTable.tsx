import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";

import type { Buchung } from "@/vermietung/types/Buchung";

import { buchungColumnsTanstack } from "@/vermietung/components/anmeldung/buchungColumnsTanstack";

interface Props {
  data: Buchung[];

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

export function BuchungTable({ data, selectedId, onSelect, sorting, onSortingChange }: Props) {
  return (
    <GenericTableTanstack
      data={data}
      columns={buchungColumnsTanstack}
      selectedRowId={selectedId}
      onSelectRow={(row) => onSelect(row ?? null)}
      sorting={sorting}
      onSortingChange={onSortingChange}
      mobileRenderRow={(row) => (
        <div>
          <strong>{row.buchungsnummer}</strong>

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
