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
      const message = selectedVerein.name + " wurde gelöscht!";
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
      const message = selectedVerein.name + " wurde nicht gelöscht!";
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

  const placeholderFunction = (): boolean => {
    return false;
  };

  btnÄndernVerein = !selectedVerein; // true if selectedVerein is not null, false otherwise
  btnLöschenVerein = !selectedVerein;

  return (
    <>
      <Toast ref={toast} />
      <div className="grid m-auto w-11">
        {selectedVerein && (
          <>
            {FormFeld(
              selectedVerein.abk,
              "Kurzname",
              true,
              placeholderFunction
            )}
            {FormFeld(selectedVerein.name, "Verein", true, placeholderFunction)}
            {FormFeld(
              selectedVerein.strasse,
              "Strasse",
              true,
              placeholderFunction
            )}
            {FormFeld(selectedVerein.plz, "PLZ", true, placeholderFunction)}
            {FormFeld(selectedVerein.ort, "Ort", true, placeholderFunction)}
            {FormFeld(
              selectedVerein.bankName,
              "Bank",
              true,
              placeholderFunction
            )}
            {FormFeld(
              selectedVerein.kontoInhaber,
              "Kontoinhaber",
              true,
              placeholderFunction
            )}
            {FormFeld(
              selectedVerein.kiAnschrift,
              "Anschrift",
              true,
              placeholderFunction
            )}
            {FormFeld(selectedVerein.iban, "IBAN", true, placeholderFunction)}
            {FormFeld(selectedVerein.bic, "BIC", true, placeholderFunction)}
          </>
        )}
      </div>
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
    </>
  );
};
