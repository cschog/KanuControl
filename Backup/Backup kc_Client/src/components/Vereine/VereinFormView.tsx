import React, { useState, useCallback, useRef } from "react";
import { Toast } from "primereact/toast";
import { Button } from "primereact/button";
import { ConfirmDialog } from "primereact/confirmdialog";
import  VereinDTO  from "../interfaces/VereinDTO";

interface VereinFormViewProps {
  onNeuerVerein: () => void; // Callback to create a new Verein
  btnNeuerVerein: boolean; // Disable or enable the "New" button
  onÄndernVerein: () => void; // Callback to edit a Verein
  btnÄndernVerein: boolean; // Disable or enable the "Edit" button
  onDeleteVerein: () => void; // Callback to delete a Verein
  btnLöschenVerein: boolean; // Disable or enable the "Delete" button
  onStartMenue: () => void; // Callback to navigate back to the menu
  selectedVerein: VereinDTO | null; // Currently selected Verein
}

export const VereinFormView: React.FC<VereinFormViewProps> = ({
  onNeuerVerein,
  btnNeuerVerein,
  onÄndernVerein,
  btnÄndernVerein,
  onDeleteVerein,
  btnLöschenVerein,
  onStartMenue,
  selectedVerein,
}) => {
  const toast = useRef<Toast>(null);
  const [visible, setVisible] = useState(false); // For delete confirmation dialog

  const acceptDelete = useCallback(() => {
    if (selectedVerein) {
      const message = `Der Verein ${selectedVerein.name} wurde gelöscht!`;
      onDeleteVerein();
      if (toast.current) {
        toast.current.show({
          severity: "info",
          summary: "Bestätigung",
          detail: message,
          life: 3000,
        });
      }
    }
  }, [selectedVerein, onDeleteVerein]);

  const rejectDelete = useCallback(() => {
    if (toast.current) {
      toast.current.show({
        severity: "warn",
        summary: "Abbruch",
        detail: "Löschen wurde abgebrochen.",
        life: 3000,
      });
    }
  }, []);

  const renderVereinDetails = () => {
    if (!selectedVerein) {
      return <p>Kein Verein ausgewählt.</p>;
    }

    return (
      <div>
        <h3>Details:</h3>
        <p>
          <strong>Vereinsname:</strong> {selectedVerein.name}
        </p>
        <p>
          <strong>Abkürzung:</strong> {selectedVerein.abk}
        </p>
        <p>
          <strong>Straße:</strong> {selectedVerein.strasse || "Nicht angegeben"}
        </p>
        <p>
          <strong>PLZ:</strong> {selectedVerein.plz || "Nicht angegeben"}
        </p>
        <p>
          <strong>Ort:</strong> {selectedVerein.ort || "Nicht angegeben"}
        </p>
        <p>
          <strong>Telefon:</strong> {selectedVerein.telefon || "Nicht angegeben"}
        </p>
        <p>
          <strong>Bankname:</strong> {selectedVerein.bankName || "Nicht angegeben"}
        </p>
        <p>
          <strong>Konto Inhaber:</strong> {selectedVerein.kontoInhaber || "Nicht angegeben"}
        </p>
        <p>
          <strong>IBAN:</strong> {selectedVerein.iban || "Nicht angegeben"}
        </p>
        <p>
          <strong>BIC:</strong> {selectedVerein.bic || "Nicht angegeben"}
        </p>
      </div>
    );
  };

  return (
    <>
      <Toast ref={toast} />
      <div className="form-view">
        <div className="details-section">{renderVereinDetails()}</div>
        <div className="button-section">
          <Button
            label="Neuer Verein"
            className="p-button-outlined m-2"
            onClick={onNeuerVerein}
            disabled={btnNeuerVerein}
          />
          <Button
            label="Ändern"
            className="p-button-outlined p-button-warning m-2"
            onClick={onÄndernVerein}
            disabled={btnÄndernVerein}
          />
          <Button
            label="Löschen"
            className="p-button-outlined p-button-danger m-2"
            onClick={() => setVisible(true)}
            disabled={btnLöschenVerein}
          />
          <Button
            label="Zurück"
            className="p-button-outlined m-2"
            onClick={onStartMenue}
          />
        </div>
        <ConfirmDialog
          visible={visible}
          onHide={() => setVisible(false)}
          message={`Möchten Sie den Verein ${
            selectedVerein ? selectedVerein.name : ""
          } wirklich löschen?`}
          header="Bestätigung erforderlich"
          icon="pi pi-exclamation-triangle"
          accept={acceptDelete}
          reject={rejectDelete}
        />
      </div>
    </>
  );
};