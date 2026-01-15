import React, { ReactNode } from "react";
import { BtnEditDeleteBack } from "@/components/common/BtnEditDeleteBack";
import { Button } from "primereact/button";
import { ConfirmDialog } from "primereact/confirmdialog";
import { Person } from "@/api/types/Person"; // Import the Person interface

interface ButtonNeuePersonProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;
  visible: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  selectedPerson: Person | null; // Use the Person interface here
  accept: () => void;
  reject: () => void;
  onÄndernPerson: () => void;
  btnÄndernPerson: boolean;
  btnLöschenPerson: boolean;
  onStartMenue: () => void;
}

export function buttonNeuePerson({
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
}: ButtonNeuePersonProps): ReactNode {
  return (
    <div>
      <Button
        label="Neue Person" // Update the label
        className="p-button-outlined m-2"
        onClick={onNeuePerson}
        disabled={btnNeuePerson}
      />
      <ConfirmDialog
        visible={visible}
        onHide={() => setVisible(false)}
        message={
          selectedPerson
            ? "Soll die Person " + selectedPerson.name + " gelöscht werden?" // Update the message
            : ""
        }
        header={
          selectedPerson ? "Löschen der Person " + selectedPerson.name + "?" : "" // Update the header
        }
        icon="pi pi-exclamation-triangle"
        accept={accept}
        reject={reject}
      />
      <BtnEditDeleteBack
        onÄndern={onÄndernPerson}
        btnÄndern={btnÄndernPerson}
        setVisible={setVisible}
        btnLöschen={btnLöschenPerson}
        onStartMenue={onStartMenue}
      />
    </div>
  );
}
