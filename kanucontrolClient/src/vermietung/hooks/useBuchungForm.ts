import { useEffect, useState } from "react";

import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";

const createInitialForm = (): BuchungSave => ({
  anreise: "",
  abreise: "",
  mietobjektId: 0,
  mietbereichIds: [],
  mieterId: 0,
  veranstalterVereinId: undefined,
  status: "ANFRAGE",
});

export function useBuchungForm(buchung: Buchung | null) {
  const { mietobjekt } = useVermietungContext();

  const [form, setForm] = useState<BuchungSave>(createInitialForm());

  const [mieter, setMieter] = useState<PersonRef | undefined>();
  const [veranstalter, setVeranstalter] = useState<VereinRef | undefined>();
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);

  useEffect(() => {
    if (buchung) {
      setForm({
        anreise: buchung.anreise,
        abreise: buchung.abreise,
        mietobjektId: buchung.mietobjektId,
        mietbereichIds: buchung.mietbereichIds ?? [],
        mieterId: buchung.mieterId,
        veranstalterVereinId: buchung.veranstalterVereinId,
        status: buchung.status,
      });

      return;
    }

    if (mietobjekt) {
      setForm({
        ...createInitialForm(),
        mietobjektId: mietobjekt.id,
      });
    } else {
      setForm(createInitialForm());
    }
  }, [buchung, mietobjekt]);

  useEffect(() => {
    const mietobjektId = buchung?.mietobjektId ?? mietobjekt?.id;

    if (!mietobjektId) {
      setMietbereiche([]);
      return;
    }

    const loadMietbereiche = async () => {
      try {
        const data = await getMietbereiche(mietobjektId);

        setMietbereiche(data.filter((mietbereich) => mietbereich.mietbar));
      } catch (error) {
        console.error("Fehler beim Laden der Mietbereiche:", error);
        setMietbereiche([]);
      }
    };

    void loadMietbereiche();
  }, [buchung?.mietobjektId, mietobjekt?.id]);

  const update = <K extends keyof BuchungSave>(key: K, value: BuchungSave[K]) => {
    setForm((current) =>
      current
        ? {
            ...current,
            [key]: value,
          }
        : current,
    );
  };

  const handleMieterChange = (value?: PersonRef) => {
    setMieter(value);
    update("mieterId", value?.id ?? 0);
  };

  const handleVeranstalterChange = (value?: VereinRef) => {
    setVeranstalter(value);
    update("veranstalterVereinId", value?.id);
  };

  const buildSavePayload = (): BuchungSave | null => {
    if (!form.anreise || !form.abreise) return null;
    if (form.abreise < form.anreise) return null;

    return form;
  };

  return {
    form,
    update,
    mietbereiche,
    mieter,
    veranstalter,
    setMieter: handleMieterChange,
    setVeranstalter: handleVeranstalterChange,
    buildSavePayload,
  };
}
