import Verein from "@/api/types/VereinFormModel";
import { VereinSave } from "@/api/types/VereinSave";
import { useEntityForm } from "@/components/common/hooks/useEntityForm";

function mapDetailToSave(v: Verein): VereinSave {
  return {
    id: v.id,
    name: v.name,
    abk: v.abk,
    strasse: v.strasse,
    plz: v.plz,
    ort: v.ort,
    telefon: v.telefon,
    bankName: v.bankName,
    iban: v.iban,
    kontoinhaberId: v.kontoinhaber?.id,
  };
}

function emptyVerein(): VereinSave {
  return {
    name: "",
    abk: "",
    strasse: "",
    plz: "",
    ort: "",
    telefon: "",
    bankName: "",
    iban: "",
  };
}

export function useVereinForm(initial?: Verein | null) {
  return useEntityForm<Verein, VereinSave>(
    initial,
    mapDetailToSave,
    emptyVerein,
    (form) => form,
    (form) => form.name.trim().length >= 2 && form.abk.trim().length >= 1,
  );
}
