import React from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Verein } from "@/api/types/Verein";

interface DataTableRowSelectEvent {
	originalEvent: React.SyntheticEvent;
	data: Verein;
	type: "row" | "radio" | "checkbox";
}

interface VereinTableProps {
	data: Verein[];
	selectedVerein: Verein | null; // Use `null` as initial selection value
	handleRowSelect: (e: DataTableRowSelectEvent) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({
	data,
	selectedVerein,
	handleRowSelect,
  }) => {
	return (
	  <div className="card border-solid m-auto w-full overflow-auto">
		<DataTable
		  value={data}
		  size="small"
		  selectionMode="single"
		  selection={selectedVerein}
		  sortField="abk"
		  sortOrder={1}
		  scrollable
		  scrollHeight="400px" // Set appropriate vertical height
		  tableStyle={{ tableLayout: "auto", width: "100%" }} // Ensure full width
		  className="p-datatable-gridlines"
		  onRowSelect={handleRowSelect}
		>
		  <Column
			field="abk"
			header="Abk."
			style={{
			  minWidth: "60px",
			}}
			sortable
		  ></Column>
		  <Column
			field="name"
			header="Verein"
			style={{
			  minWidth: "150px",
			  maxWidth: "300px", // Optional to limit maximum width
			}}
			sortable
		  ></Column>
		  <Column
			field="strasse"
			header="Strasse"
			style={{
			  minWidth: "150px",
			}}
		  ></Column>
		  <Column
			field="ort"
			header="Ort"
			style={{
			  minWidth: "100px",
			}}
			sortable
		  ></Column>
		</DataTable>
	  </div>
	);
  };