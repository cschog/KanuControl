import React, { useState, useCallback, useRef } from "react";
import { Toast } from "primereact/toast";
import { FormFeld } from "../../services/FormFeld";
import { buttonNeuePerson } from "./BtnNeuePerson"; // Assuming you have a similar button component for new persons
import { Person } from "../interfaces/Person"; // Import the Person interface

interface PersonFormViewProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;
  onÄndernPerson: () => void;
  btnÄndernPerson: boolean;
  onDeletePerson: () => void;
  btnLöschenPerson: boolean;
  onStartMenue: () => void;
  selectedPerson: Person | null; // Use the Person interface here
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
      const message = selectedPerson.name + " wurde gelöscht!";
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
      const message = selectedPerson.name + " wurde nicht gelöscht!";
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

  const placeholderFunction = (): boolean => {
    return false;
  };

  btnÄndernPerson = !selectedPerson; // true if selectedPerson is not null, false otherwise
  btnLöschenPerson = !selectedPerson;

  return (
    <>
      <Toast ref={toast} />
      <div className="grid m-auto w-11">
        {selectedPerson && (
          <>
            {FormFeld(selectedPerson.name, "Name", true, placeholderFunction)}
            {FormFeld(selectedPerson.vorname, "Vorname", true, placeholderFunction)}
            {FormFeld(selectedPerson.strasse, "Strasse", true, placeholderFunction)}
            {FormFeld(selectedPerson.plz, "PLZ", true, placeholderFunction)}
            {FormFeld(selectedPerson.ort, "Ort", true, placeholderFunction)}
            {FormFeld(selectedPerson.telefon, "Telefon", true, placeholderFunction)}
            {FormFeld(selectedPerson.bankName, "Bank", true, placeholderFunction)}
            {FormFeld(selectedPerson.iban, "IBAN", true, placeholderFunction)}
            {FormFeld(selectedPerson.bic, "BIC", true, placeholderFunction)}
          </>
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
