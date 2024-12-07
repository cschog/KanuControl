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
  const isBtnÄndernPersonDisabled = !selectedPerson; // true if selectedPerson is null
  const isBtnLöschenPersonDisabled = !selectedPerson;

  return (
    <>
      <Toast ref={toast} />
      <div className="grid m-auto w-11">
        {selectedPerson && (
          <>
            <FormFeld
              value={selectedPerson.name}
              label="Name"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
            <FormFeld
              value={selectedPerson.vorname}
              label="Vorname"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
            <FormFeld
              value={selectedPerson.strasse}
              label="Strasse"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
             <FormFeld
              value={selectedPerson.plz}
              label="PLZ"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
             <FormFeld
              value={selectedPerson.ort}
              label="Ort"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
             <FormFeld
              value={selectedPerson.telefon}
              label="Telefon"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
            <FormFeld
              value={selectedPerson.bankName}
              label="Bank"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
             <FormFeld
              value={selectedPerson.iban}
              label="IBAN"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
             <FormFeld
              value={selectedPerson.bic}
              label="BIC"
              disabled={true}
              onChange={(value) => console.log(value)}
            />
           
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
        btnÄndernPerson: isBtnÄndernPersonDisabled,
        btnLöschenPerson: isBtnLöschenPersonDisabled,
        onStartMenue,
      })}
    </>
  );
};
