import { useState, useRef, useCallback, useEffect } from "react";
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

  const createUpdateVerein = useCallback(() => {
    if (verein) {
      const updatedVerein: Verein = {
        id: verein.id,
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
    verein,
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
    return false;
  };

  return (
    <>
      <Toast ref={toast} />
      <div className="w-full max-w-4xl mx-auto bg-white shadow-lg rounded-lg p-6">
        <h2 className="text-xl font-bold mb-6 text-center">
          {verein?.id ? "Verein bearbeiten" : "Neuen Verein erstellen"}
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <FormFeld
            value={abk}
            label="Abkürzung"
            disabled={false}
            onChange={setVereinsKurz}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={name}
            label="Vereinsname"
            disabled={false}
            onChange={setVereinsName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={strasse}
            label="Strasse"
            disabled={false}
            onChange={setVereinsStrasse}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={plz}
            label="PLZ"
            disabled={false}
            onChange={setVereinsPLZ}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={ort}
            label="Ort"
            disabled={false}
            onChange={setVereinsOrt}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={telefon}
            label="Telefon"
            disabled={false}
            onChange={setVereinsTelefon}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={bankName}
            label="Bank"
            disabled={false}
            onChange={setVereinsBankName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={kontoInhaber}
            label="Konto-Inhaber"
            disabled={false}
            onChange={setVereinsKontoInhaber}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={kiAnschrift}
            label="Anschrift"
            disabled={false}
            onChange={setVereinsKIAnschrift}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={iban}
            label="IBAN"
            disabled={false}
            onChange={setVereinsIBAN}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
          <FormFeld
            value={bic}
            label="BIC"
            disabled={false}
            onChange={setVereinsBIC}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded"
          />
        </div>
        <div className="mt-6 flex justify-end space-x-4">
          <BtnStoreCancel
            createUpdate={handleCreateUpdateVerein}
            onAbbruch={onAbbruch}
          />
        </div>
      </div>
    </>
  );
}