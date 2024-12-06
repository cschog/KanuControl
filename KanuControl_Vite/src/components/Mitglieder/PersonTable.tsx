import React from "react";
import { DataTable, DataTableSelectParams } from "primereact/datatable";
import { Column } from "primereact/column";
import { Person } from "../interfaces/Person"; // Import the Person interface

interface PersonTableProps {
  data: Person[];
  selectedPerson: Person | null; // Use `null` as initial selection value
  handleRowSelect: (e: DataTableSelectParams) => void;
}

export const PersonTable: React.FC<PersonTableProps> = ({
  data,
  selectedPerson,
  handleRowSelect,
}) => {
  return (
    <div className="card border-solid m-auto w-11">
      <DataTable
        value={data}
        size="small"
        selectionMode="single"
        selection={selectedPerson}
        sortField="vorname"
        sortOrder={1}
        scrollable
        scrollHeight="35vh"
        onRowSelect={handleRowSelect}>
        <Column
          field="vorname"
          header="Vorname"
          style={{
            minWidth: "100px",
          }}
          sortable></Column>
        <Column
          field="name"
          header="Nachname"
          style={{
            minWidth: "200px",
          }}
          sortable></Column>
        <Column
          field="strasse"
          header="Straße"
          style={{
            minWidth: "100px",
          }}></Column>
        <Column
          field="plz"
          header="PLZ"
          style={{
            minWidth: "80px",
          }}
          sortable></Column>
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
