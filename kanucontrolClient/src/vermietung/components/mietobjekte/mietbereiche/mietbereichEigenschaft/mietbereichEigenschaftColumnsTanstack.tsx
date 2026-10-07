// src/vermietung/components/mietobjekte/mietbereiche/mietbereichEigenschaft/mietbereichEigenschaftColumnsTanstack.tsx
import type { ColumnDef } from "@tanstack/react-table";

import type { MietbereichEigenschaft } from "@/vermietung/types/MietbereichEigenschaft";

const typLabels: Record<MietbereichEigenschaft["typ"], string> = {
  TEXT: "Text",
  ZAHL: "Zahl",
  FLAECHE: "Fläche",
  HOEHE: "Höhe",
  LAENGE: "Länge",
  BOOLEAN: "Ja / Nein",
};

export const mietbereichEigenschaftColumnsTanstack: ColumnDef<MietbereichEigenschaft>[] = [
  {
    accessorKey: "sortierung",
    header: "Nr.",
  },
  {
    accessorKey: "bezeichnung",
    header: "Eigenschaft",
  },
  {
    accessorKey: "typ",
    header: "Typ",
    cell: ({ row }) => typLabels[row.original.typ],
  },
  {
    accessorKey: "wert",
    header: "Wert",
    cell: ({ row }) => {
      const eigenschaft = row.original;

      switch (eigenschaft.typ) {
        case "FLAECHE":
          return `${eigenschaft.wert} m²`;

        case "HOEHE":
        case "LAENGE":
          return `${Number(eigenschaft.wert).toLocaleString("de-DE")} m`;

        case "BOOLEAN":
          return eigenschaft.wert === "true" ? "Ja" : "Nein";

        default:
          return eigenschaft.wert;
      }
    },
  },
];
