import React from "react";
import { DataTable, DataTableSelectParams } from "primereact/datatable";
import { Column } from "primereact/column";
import { Verein } from "../interfaces/Verein";

interface VereinTableProps {
  data: Verein[];
  selectedVerein: Verein | null; // Use `null` as initial selection value
  handleRowSelect: (e: DataTableSelectParams) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({
  data,
  selectedVerein,
  handleRowSelect,
}) => {
  return (
    <div className="card border-solid m-auto w-11">
      <DataTable
        value={data}
        size="small"
        selectionMode="single"
        selection={selectedVerein}
        sortField="abk"
        sortOrder={1}
        scrollable
        scrollHeight="35vh"
        onRowSelect={handleRowSelect}>
        {/* ... Columns ... */}
        <Column
          field="abk"
          header="Abk."
          style={{
            minWidth: "30px",
          }}
          sortable></Column>
        <Column
          field="name"
          header="Verein"
          style={{
            minWidth: "200px",
          }}
          sortable></Column>
        <Column
          field="strasse"
          header="Strasse"
          style={{
            minWidth: "100px",
          }}></Column>
        <Column
          field="ort"
          header="Ort"
          style={{
            minWidth: "100px",
          }}
          sortable></Column>
      </DataTable>
    </div>
  );
};
