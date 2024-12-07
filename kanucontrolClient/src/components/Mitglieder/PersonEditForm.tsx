import { useState, useRef, useCallback, useEffect } from "react";
import { Toast } from "primereact/toast";
import { BtnStoreCancel } from "../../services/BtnStoreCancel";
import { FormFeld } from "../../services/FormFeld";
import { Person } from "../interfaces/Person"; // Import the Person interface

interface PersonEditFormProps {
	onSpeichern: (person: Person) => void;
	onAbbruch: () => void;
	person?: Person; // Use the Person interface here
}

export function PersonEditForm({
	onSpeichern,
	onAbbruch,
	person,
}: Readonly<PersonEditFormProps>) {
	const [name, setPersonName] = useState("");
	const [vorname, setPersonVorname] = useState("");
	const [strasse, setPersonStrasse] = useState("");
	const [plz, setPersonPLZ] = useState("");
	const [ort, setPersonOrt] = useState("");
	const [telefon, setPersonTelefon] = useState("");
	const [bankName, setPersonBankName] = useState("");
	const [iban, setPersonIBAN] = useState("");
	const [bic, setPersonBIC] = useState("");
	const toast = useRef<Toast | null>(null);

	const createUpdatePerson = useCallback(() => {
		if (person) {
			const updatedPerson: Person = {
				id: person.id, // Include the id
				name,
				vorname,
				strasse,
				plz,
				ort,
				telefon,
				bankName,
				iban,
				bic,
			};
			onSpeichern(updatedPerson);
		}
	}, [
		name,
		vorname,
		strasse,
		plz,
		ort,
		telefon,
		bankName,
		iban,
		bic,
		onSpeichern,
		person,
	]);

	useEffect(() => {
		if (person) {
			setPersonName(person.name);
			setPersonVorname(person.vorname);
			setPersonStrasse(person.strasse);
			setPersonPLZ(person.plz);
			setPersonOrt(person.ort);
			setPersonTelefon(person.telefon);
			setPersonBankName(person.bankName);
			setPersonIBAN(person.iban);
			setPersonBIC(person.bic);
		}
	}, [person]);

	const handleCreateUpdatePerson = () => {
		createUpdatePerson();
		return false; // Placeholder value indicating that the update was not successful
	};

	return (
		<>
			<Toast ref={toast} />
			<div className="grid m-auto w-11">
				<FormFeld
				value={name}
				label="Name"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={vorname}
				label="Vorname"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
			  <FormFeld
				value={strasse}
				label="Strasse"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={plz}
				label="PLZ"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={ort}
				label="Ort"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={telefon}
				label="Telefon"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={bankName}
				label="Bank"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={iban}
				label="IBAN"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				<FormFeld
				value={bic}
				label="BIC"
				disabled={false}
				onChange={(value) => console.log(value)}
			  />
				
			</div>
			<BtnStoreCancel
				createUpdate={handleCreateUpdatePerson}
				onAbbruch={onAbbruch}
			/>
		</>
	);
}
