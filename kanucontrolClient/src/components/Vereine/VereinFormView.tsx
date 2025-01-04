import React, { useState, useCallback, useRef } from "react";
import { Toast } from "primereact/toast";
import { FormFeld } from "../../services/FormFeld";
import { buttonNeuerVerein } from "./BtnNeuerVerein";
import { Verein } from "../interfaces/Verein";

interface VereinFormViewProps {
  onNeuerVerein: () => void;
  btnNeuerVerein: boolean;
  onÄndernVerein: () => void;
  btnÄndernVerein: boolean;
  onDeleteVerein: () => void;
  btnLöschenVerein: boolean;
  onStartMenue: () => void;
  selectedVerein: Verein | null;
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
  const [visible, setVisible] = useState(false);

  // Called when user confirms deletion
  const accept = useCallback(() => {
    if (selectedVerein) {
      const message = `${selectedVerein.name} wurde gelöscht!`;
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

  // Called when user cancels deletion
  const reject = useCallback(() => {
    if (selectedVerein) {
      const message = `${selectedVerein.name} wurde nicht gelöscht!`;
      if (toast.current) {
        toast.current.show({
          severity: "warn",
          summary: "Abbruch",
          detail: message,
          life: 3000,
        });
      }
    }
  }, [selectedVerein]);

  return (
    <>
      <Toast ref={toast} />

      {/* Same container layout as in VereinEditForm */}
      <div className="w-full max-w-4xl mx-auto bg-white shadow-lg rounded-lg p-6">
        <h2 className="text-xl font-bold mb-6 text-center text-blue-700">
          {/* Possibly a heading here */}
          Vereinsdetails
        </h2>

        {selectedVerein ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-5 gap-y-2">
            <FormFeld
              value={selectedVerein.abk}
              label="Abkürzung"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.name}
              label="Verein"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.strasse}
              label="Strasse"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.plz}
              label="PLZ"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.ort}
              label="Ort"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.telefon}
              label="Telefon"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.bankName}
              label="Bank"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.kontoInhaber}
              label="Konto-Inhaber"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.kiAnschrift}
              label="Anschrift"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.iban}
              label="IBAN"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
            <FormFeld
              value={selectedVerein.bic}
              label="BIC"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 
                         focus:border-blue-500 rounded p-2 w-full"
            />
          </div>
        ) : (
          <div className="text-gray-500 italic text-center">
            Bitte wählen Sie einen Verein aus der Tabelle aus.
          </div>
        )}
      </div>

      {/* Buttons for Neuer/Löschen/Ändern, etc. */}
      <div className="mt-4">
        {buttonNeuerVerein({
          onNeuerVerein,
          btnNeuerVerein,
          visible,
          setVisible,
          selectedVerein,
          accept,
          reject,
          onÄndernVerein,
          btnÄndernVerein,
          btnLöschenVerein,
          onStartMenue,
        })}
      </div>
    </>
  );
};