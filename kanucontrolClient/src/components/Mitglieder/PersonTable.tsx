import React from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Person } from "../interfaces/Person"; // Import the Person interface

interface DataTableRowSelectEvent {
	originalEvent: React.SyntheticEvent;
	data: Person;
	type: "row" | "radio" | "checkbox";
}

interface PersonTableProps {
	data: Person[];
	selectedPerson: Person | null;
	handleRowSelect: (e: DataTableRowSelectEvent) => void;
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
