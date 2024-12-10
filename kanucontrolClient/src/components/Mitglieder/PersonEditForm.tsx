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
  const [name, setName] = useState("");
  const [vorname, setVorname] = useState("");
  const [strasse, setStrasse] = useState("");
  const [plz, setPLZ] = useState("");
  const [ort, setOrt] = useState("");
  const [telefon, setTelefon] = useState("");
  const [bankName, setBankName] = useState("");
  const [iban, setIBAN] = useState("");
  const [bic, setBIC] = useState("");
  const toast = useRef<Toast | null>(null);

  const createUpdatePerson = useCallback(() => {
	try {
		if (person) {
			const updatedPerson: Person = {
				id: person.id,
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
			return true; // Indicate success
		}
		return false; // Indicate failure if person is not defined
	} catch (error) {
		console.error("Error updating person:", error);
		return false; // Indicate failure on exception
	}
}, [person, name, vorname, strasse, plz, ort, telefon, bankName, iban, bic, onSpeichern]);

  useEffect(() => {
    if (person) {
      setName(person.name);
      setVorname(person.vorname);
      setStrasse(person.strasse);
      setPLZ(person.plz);
      setOrt(person.ort);
      setTelefon(person.telefon);
      setBankName(person.bankName);
      setIBAN(person.iban);
      setBIC(person.bic);
    }
  }, [person]);

  const handleCreateUpdatePerson = () => {
    createUpdatePerson();
    return false; // Placeholder value indicating that the update was not successful
  };

  return (
    <>
      <Toast ref={toast} />
      <div className="w-full max-w-4xl mx-auto bg-white shadow-lg rounded-lg p-6">
        <h2 className="text-xl font-bold mb-6 text-center">
          {person?.id ? "Mitglied bearbeiten" : "Neues Mitglied erstellen"}
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-5 gap-y-2">
          <FormFeld
            value={name}
            label="Name"
            disabled={false}
            onChange={setName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={vorname}
            label="Vorname"
            disabled={false}
            onChange={setVorname}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={strasse}
            label="Strasse"
            disabled={false}
            onChange={setStrasse}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={plz}
            label="PLZ"
            disabled={false}
            onChange={setPLZ}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={ort}
            label="Ort"
            disabled={false}
            onChange={setOrt}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={telefon}
            label="Telefon"
            disabled={false}
            onChange={setTelefon}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={bankName}
            label="Bank"
            disabled={false}
            onChange={setBankName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={iban}
            label="IBAN"
            disabled={false}
            onChange={setIBAN}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2  w-full min-w-[250px]"
          />
          <FormFeld
            value={bic}
            label="BIC"
            disabled={false}
            onChange={setBIC}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
        </div>
        <div className="mt-6 flex justify-end space-x-4">
          <BtnStoreCancel
            createUpdate={handleCreateUpdatePerson}
            onAbbruch={onAbbruch}
          />
        </div>
      </div>
    </>
  );
}
