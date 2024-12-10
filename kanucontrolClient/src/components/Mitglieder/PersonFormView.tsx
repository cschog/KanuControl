import React, { useState, useCallback, useRef } from "react";
import { Toast } from "primereact/toast";
import { FormFeld } from "../../services/FormFeld";
import { buttonNeuePerson } from "./BtnNeuePerson"; // Assuming you have a similar button component for new persons
import { Person } from "../interfaces/Person"; // Import the Person interface

interface PersonFormViewProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;
  onÄndernPerson: () => void;
  btnÄndernPerson: boolean; // Correct prop name
  onDeletePerson: () => void;
  btnLöschenPerson: boolean;
  onStartMenue: () => void;
  selectedPerson: Person | null;
}

export const PersonFormView: React.FC<PersonFormViewProps> = ({
  onNeuePerson,
  btnNeuePerson,
  onÄndernPerson,
  btnÄndernPerson,
  onDeletePerson,
  btnLöschenPerson,
  onStartMenue,
  selectedPerson,
}) => {
  const toast = useRef<Toast>(null);

  // Proper destructuring of useState
  const [visible, setVisible] = useState(false);

  const accept = useCallback(() => {
    if (selectedPerson) {
      const message = `${selectedPerson.name} wurde gelöscht!`;
      onDeletePerson();
      if (toast.current) {
        toast.current.show({
          severity: "info",
          summary: "Bestätigung",
          detail: message,
          life: 3000,
        });
      }
    }
  }, [selectedPerson, onDeletePerson]);

  const reject = useCallback(() => {
    if (selectedPerson) {
      const message = `${selectedPerson.name} wurde nicht gelöscht!`;
      if (toast.current) {
        toast.current.show({
          severity: "warn",
          summary: "Abbruch",
          detail: message,
          life: 3000,
        });
      }
    }
  }, [selectedPerson]);

  // Use local variables to determine the button states

  return (
    <>
      <Toast ref={toast} />
      <div className="grid m-auto w-11 p-4 border bg-gray-50 shadow-md rounded">
      <h2 className="text-xl font-bold mb-4 text-blue-700">Mitgliederdetails</h2>
        {selectedPerson ? (
          <>
            <FormFeld
              value={selectedPerson.name}
              label="Name"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedPerson.vorname}
              label="Vorname"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedPerson.strasse}
              label="Strasse"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
             <FormFeld
              value={selectedPerson.plz}
              label="PLZ"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
             <FormFeld
              value={selectedPerson.ort}
              label="Ort"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
             <FormFeld
              value={selectedPerson.telefon}
              label="Telefon"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedPerson.bankName}
              label="Bank"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
             <FormFeld
              value={selectedPerson.iban}
              label="IBAN"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
             <FormFeld
              value={selectedPerson.bic}
              label="BIC"
              disabled={true}
              onChange={(value) => console.log(value)}
               className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
          </>
        ): (
          <div className="text-gray-500 italic text-center">
            Bitte wählen Sie ein Mitglied aus der Tabelle aus.
          </div>
        )}
      </div>
      {buttonNeuePerson({
        onNeuePerson,
        btnNeuePerson,
        visible,
        setVisible,
        selectedPerson,
        accept,
        reject,
        onÄndernPerson,
        btnÄndernPerson,
        btnLöschenPerson,
        onStartMenue,
      })}
    </>
  );
};
