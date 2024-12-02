import React, { useState, useEffect } from "react";
import VereinDTO from "../interfaces/VereinDTO";
import { BtnStoreCancel } from "../../services/BtnStoreCancel";

interface VereinEditFormProps {
  onSpeichern: (verein: VereinDTO) => void; // Callback for saving
  onAbbruch: () => void; // Callback for canceling
  verein?: VereinDTO; // Optional: For editing an existing Verein
}

export const VereinEditForm: React.FC<VereinEditFormProps> = ({
  onSpeichern,
  onAbbruch,
  verein,
}) => {
  const [formState, setFormState] = useState<VereinDTO>({
    name: "",
    abk: "",
    strasse: "",
    plz: "",
    ort: "",
    telefon: "",
    bankName: "",
    kontoInhaber: "",
    kiAnschrift: "",
    iban: "",
    bic: "",
  });

  const [isValid, setIsValid] = useState(false);

  // Populate form state if editing an existing Verein
  useEffect(() => {
    if (verein) {
      setFormState(verein);
    }
  }, [verein]);

  // Validate form: Check required fields
  useEffect(() => {
    const { name, abk } = formState;
    setIsValid(name.trim() !== "" && abk.trim() !== "");
  }, [formState]);

  // Handle input changes
  const handleChange = (field: keyof VereinDTO, value: string) => {
    setFormState((prev) => ({ ...prev, [field]: value }));
  };

  // Handle form submission
  const handleSubmit = (): boolean => {
    if (isValid) {
      onSpeichern(formState); // Save the form
      return true; // Indicate success
    }
    return false; // Indicate failure (e.g., invalid form state)
  };

  return (
    <div className="form-container">
      <h2>{verein ? "Verein bearbeiten" : "Neuen Verein erstellen"}</h2>
      <form>
        <div className="form-group">
          <label htmlFor="name">Vereinsname</label>
          <input
            type="text"
            id="name"
            value={formState.name}
            onChange={(e) => handleChange("name", e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="abk">Abkürzung</label>
          <input
            type="text"
            id="abk"
            value={formState.abk}
            onChange={(e) => handleChange("abk", e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="strasse">Straße</label>
          <input
            type="text"
            id="strasse"
            value={formState.strasse}
            onChange={(e) => handleChange("strasse", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="plz">PLZ</label>
          <input
            type="text"
            id="plz"
            value={formState.plz}
            onChange={(e) => handleChange("plz", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="ort">Ort</label>
          <input
            type="text"
            id="ort"
            value={formState.ort}
            onChange={(e) => handleChange("ort", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="telefon">Telefon</label>
          <input
            type="text"
            id="telefon"
            value={formState.telefon}
            onChange={(e) => handleChange("telefon", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="bankName">Bankname</label>
          <input
            type="text"
            id="bankName"
            value={formState.bankName}
            onChange={(e) => handleChange("bankName", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="kontoInhaber">Kontoinhaber</label>
          <input
            type="text"
            id="kontoInhaber"
            value={formState.kontoInhaber}
            onChange={(e) => handleChange("kontoInhaber", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="kiAnschrift">Kontoinhaber Anschrift</label>
          <input
            type="text"
            id="kiAnschrift"
            value={formState.kiAnschrift}
            onChange={(e) => handleChange("kiAnschrift", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="iban">IBAN</label>
          <input
            type="text"
            id="iban"
            value={formState.iban}
            onChange={(e) => handleChange("iban", e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="bic">BIC</label>
          <input
            type="text"
            id="bic"
            value={formState.bic}
            onChange={(e) => handleChange("bic", e.target.value)}
          />
        </div>
      </form>

      <div className="form-actions">
        <BtnStoreCancel createUpdate={handleSubmit} onAbbruch={onAbbruch} />
      </div>
    </div>
  );
};