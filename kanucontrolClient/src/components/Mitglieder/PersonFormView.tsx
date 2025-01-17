import React, { useState, useCallback, useRef } from "react";
import { Toast } from "primereact/toast";
import { FormFeld } from "../../services/FormFeld";
import { buttonNeuePerson } from "./BtnNeuePerson"; // Similar to buttonNeuerVerein
import { Person } from "../interfaces/Person";

interface PersonFormViewProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;
  onÄndernPerson: () => void;
  btnÄndernPerson: boolean;
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

  return (
    <>
      <Toast ref={toast} />

      {/* Same container style as your edit forms */}
      <div className="w-full max-w-4xl mx-auto bg-white shadow-lg rounded-lg p-6">
        <h2 className="text-xl font-bold mb-6 text-center text-blue-700">
          Mitgliederdetails
        </h2>

        {selectedPerson ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-5 gap-y-2">
            <FormFeld
              value={selectedPerson.name}
              label="Name"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.vorname}
              label="Vorname"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.strasse}
              label="Strasse"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.plz}
              label="PLZ"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.ort}
              label="Ort"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.telefon}
              label="Telefon"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.bankName}
              label="Bank"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.iban}
              label="IBAN"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedPerson.bic}
              label="BIC"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
          </div>
        ) : (
          <div className="text-gray-500 italic text-center">
            Bitte wählen Sie ein Mitglied aus der Tabelle aus.
          </div>
        )}
      </div>

      {/* The buttons for Neue, Löschen, etc. */}
      <div className="mt-4">
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
      </div>
    </>
  );
};