import React from "react";
import { DataTable, DataTableSelectParams } from "primereact/datatable";
import { Column } from "primereact/column";
import  VereinDTO  from "../interfaces/VereinDTO";

interface VereinTableProps {
  data: VereinDTO[];
  selectedVerein: VereinDTO | null;
  onRowSelect: (verein: VereinDTO) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({ data, selectedVerein, onRowSelect }) => {
  return (
    <DataTable
      value={data}
      selectionMode="single"
      selection={selectedVerein}
      onRowSelect={(e: DataTableSelectParams) => onRowSelect(e.data)}
    >
      <Column field="abk" header="Abkürzung" sortable />
      <Column field="name" header="Name" sortable />
      <Column field="ort" header="Ort" />
    </DataTable>
  );
};