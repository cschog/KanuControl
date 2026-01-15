import { useState, useRef, useCallback, useEffect } from "react";
import { Toast } from "primereact/toast";
import { BtnStoreCancel } from "@/components/common/BtnStoreCancel";
import { FormFeld } from "@/components/common/FormFeld";
import { Verein } from "@/api/types/Verein";

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
  const [name, setName] = useState("");
  const [abk, setAbk] = useState("");
  const [strasse, setStrasse] = useState("");
  const [plz, setPLZ] = useState("");
  const [ort, setOrt] = useState("");
  const [telefon, setTelefon] = useState("");
  const [bankName, setBankName] = useState("");
  const [kontoInhaber, setKontoInhaber] = useState("");
  const [kiAnschrift, setKIAnschrift] = useState("");
  const [iban, setIBAN] = useState("");
  const [bic, setBIC] = useState("");
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
    onSpeichern,
    verein,
  ]);

  useEffect(() => {
    if (verein) {
      setName(verein.name);
      setAbk(verein.abk);
      setStrasse(verein.strasse);
      setPLZ(verein.plz);
      setOrt(verein.ort);
      setTelefon(verein.telefon);
      setBankName(verein.bankName);
      setKontoInhaber(verein.kontoInhaber);
      setKIAnschrift(verein.kiAnschrift);
      setIBAN(verein.iban);
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
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-5 gap-y-2">
          <FormFeld
            value={abk}
            label="Abkürzung"
            disabled={false}
            onChange={setAbk}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={name}
            label="Vereinsname"
            disabled={false}
            onChange={setName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={strasse}
            label="Strasse"
            disabled={false}
            onChange={setStrasse}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={plz}
            label="PLZ"
            disabled={false}
            onChange={setPLZ}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={ort}
            label="Ort"
            disabled={false}
            onChange={setOrt}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={telefon}
            label="Telefon"
            disabled={false}
            onChange={setTelefon}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={bankName}
            label="Bank"
            disabled={false}
            onChange={setBankName}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={kontoInhaber}
            label="Konto-Inhaber"
            disabled={false}
            onChange={setKontoInhaber}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
          />
          <FormFeld
            value={kiAnschrift}
            label="Anschrift"
            disabled={false}
            onChange={setKIAnschrift}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full min-w-[200px]"
          />
          <FormFeld
            value={iban}
            label="IBAN"
            disabled={false}
            onChange={setIBAN}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full min-w-[250px]"
          />
          <FormFeld
            value={bic}
            label="BIC"
            disabled={false}
            onChange={setBIC}
            className="bg-gray-100 focus:bg-white border-2 border-gray-300 focus:border-blue-500 rounded p-2 w-full"
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