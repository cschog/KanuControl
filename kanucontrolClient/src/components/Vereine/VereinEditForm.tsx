import React, { useState, useRef, useCallback, useEffect } from "react";
import { Toast } from "primereact/toast";
import { BtnStoreCancel } from "../../services/BtnStoreCancel";
import { FormFeld } from "../../services/FormFeld";
import { Verein } from "../interfaces/Verein";

interface VereinEditFormProps {
  onSpeichern: (verein: Verein) => void;
  onAbbruch: () => void;
  verein?: Verein;
}

export function VereinEditForm({
  onSpeichern,
  onAbbruch,
  verein,
}: Readonly<VereinEditFormProps>) {
  const [name, setVereinsName] = useState("");
  const [abk, setVereinsKurz] = useState("");
  const [strasse, setVereinsStrasse] = useState("");
  const [plz, setVereinsPLZ] = useState("");
  const [ort, setVereinsOrt] = useState("");
  const [telefon, setVereinsTelefon] = useState("");
  const [bankName, setVereinsBankName] = useState("");
  const [kontoInhaber, setVereinsKontoInhaber] = useState("");
  const [kiAnschrift, setVereinsKIAnschrift] = useState("");
  const [iban, setVereinsIBAN] = useState("");
  const [bic, setVereinsBIC] = useState("");
  const toast = useRef<Toast | null>(null);

  console.log(verein);

  const createUpdateVerein = useCallback(() => {
    // If verein exists, it´s an update, so include the "id" property
    if (verein) {
      const updatedVerein: Verein = {
        id: verein.id, // Include the id
        name,
        abk,
        strasse,
        plz,
        ort,
        telefon,
        bankName,
        kontoInhaber,
        kiAnschrift,
        iban,
        bic,
      };
      onSpeichern(updatedVerein);
    } else {
      // If verein doesn't exist, it's a new entry, you might handle this case differently
      // For example, create a new instance or show an error//
    }
  }, [
    name,
    abk,
    strasse,
    plz,
    ort,
    telefon,
    bankName,
    kontoInhaber,
    kiAnschrift,
    iban,
    bic,
    onSpeichern,
  ]);

  useEffect(() => {
    if (verein) {
      setVereinsName(verein.name);
      setVereinsKurz(verein.abk);
      setVereinsStrasse(verein.strasse);
      setVereinsPLZ(verein.plz);
      setVereinsOrt(verein.ort);
      setVereinsTelefon(verein.telefon);
      setVereinsBankName(verein.bankName);
      setVereinsKontoInhaber(verein.kontoInhaber);
      setVereinsKIAnschrift(verein.kiAnschrift);
      setVereinsIBAN(verein.iban);
      setVereinsBIC(verein.bic);
    }
  }, [verein]);

  const handleCreateUpdateVerein = () => {
    createUpdateVerein();
    return false; // Placeholder value indicating that the update was not successful
  };

  return (
    <>
      <Toast ref={toast} />
      <div className="grid m-auto w-11">
        {FormFeld(abk, "Abkürzung", false, setVereinsKurz)}
        {FormFeld(name, "Vereinsname", false, setVereinsName)}
        {FormFeld(strasse, "Strasse", false, setVereinsStrasse)}
        {FormFeld(plz, "PLZ", false, setVereinsPLZ)}
        {FormFeld(ort, "Ort", false, setVereinsOrt)}
        {FormFeld(telefon, "Telefon", false, setVereinsTelefon)}
        {FormFeld(bankName, "Bank", false, setVereinsBankName)}
        {FormFeld(kontoInhaber, "Konto-Inhaber", false, setVereinsKontoInhaber)}
        {FormFeld(kiAnschrift, "Anschrift", false, setVereinsKIAnschrift)}
        {FormFeld(iban, "IBAN", false, setVereinsIBAN)}
        {FormFeld(bic, "BIC", false, setVereinsBIC)}
      </div>
      <BtnStoreCancel
        createUpdate={handleCreateUpdateVerein}
        onAbbruch={onAbbruch}
      />
    </>
  );
}
