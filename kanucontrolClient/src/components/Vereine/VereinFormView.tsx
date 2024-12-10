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
      <div className="grid m-auto w-11 p-4 border bg-gray-50 shadow-md rounded">
        <h2 className="text-xl font-bold mb-4 text-blue-700">Vereinsdetails</h2>
        {selectedVerein ? (
          <>
            <FormFeld
              value={selectedVerein.abk}
              label="Kurzname"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.name}
              label="Verein"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.strasse}
              label="Strasse"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.plz}
              label="PLZ"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.ort}
              label="Ort"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.bankName}
              label="Bank"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.kontoInhaber}
              label="Kontoinhaber"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.kiAnschrift}
              label="Anschrift"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.iban}
              label="IBAN"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
            <FormFeld
              value={selectedVerein.bic}
              label="BIC"
              disabled={true}
              onChange={() => {}}
              className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2"
            />
          </>
        ) : (
          <div className="text-gray-500 italic text-center">
            Bitte wählen Sie einen Verein aus der Tabelle aus.
          </div>
        )}
      </div>
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