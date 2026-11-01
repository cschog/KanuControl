import { useEffect, useState } from "react";

import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";
import { getPersonById } from "@/core/api/services/personApi";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave, BuchungPositionSave } from "@/vermietung/types/BuchungSave";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";
import { getMietbereichRabatte } from "@/vermietung/api/mietbereichRabattApi";
import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

const createInitialForm = (buchungsquelle: Buchungsquelle = "DIREKT"): BuchungSave => ({
  anreise: "",
  abreise: "",
  unbefristet: false,
  mietobjektId: 0,
  mietbereichIds: [],
  positionen: [],
  mieterId: 0,
  veranstalterVereinId: undefined,
  status: "ANFRAGE",
  buchungsquelle,
  bemerkung: "",
  bewertung: null,
  rabattart: Rabattart.KEIN_RABATT,
  rabattwertTyp: RabattwertTyp.PROZENTUAL,
  rabattwert: 0,
});

const datumMitZeit = (datum: string, zeit: string | null | undefined): string => {
  const formatierteZeit = !zeit ? "00:00:00" : zeit.length === 5 ? `${zeit}:00` : zeit;
  return datum ? `${datum}T${formatierteZeit}` : "";
};

export function useBuchungForm(
  buchung: Buchung | null,
  neueBuchungsquelle: Buchungsquelle = "DIREKT",
  initialZeitraum?: { anreiseDatum: string; abreiseDatum: string },
) {
  const { mietobjekt } = useVermietungContext();

  const [form, setForm] = useState<BuchungSave>(createInitialForm());

  const [mieter, setMieter] = useState<PersonRef | undefined>();
  const [veranstalter, setVeranstalter] = useState<VereinRef | undefined>();
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [rabatteByMietbereich, setRabatteByMietbereich] = useState<Record<number, MietbereichRabatt[]>>({});

  useEffect(() => {
    if (buchung) {
      const positionen: BuchungPositionSave[] = (buchung.positionen ?? []).map((position) => ({
        mietbereichId: position.mietbereichId,
        anzahl: position.anzahl,
      }));

      setForm({
        anreise: buchung.anreise,
        abreise: buchung.abreise,
        unbefristet: buchung.unbefristet,
        mietobjektId: buchung.mietobjektId,
        mietbereichIds:
          buchung.mietbereichIds?.length > 0
            ? buchung.mietbereichIds
            : positionen.map((position) => position.mietbereichId),
        positionen,
        mieterId: buchung.mieterId,
        veranstalterVereinId: buchung.veranstalterVereinId,
        status: buchung.status,
        buchungsquelle: buchung.buchungsquelle,
        bemerkung: buchung.bemerkung ?? "",
        bewertung: buchung.bewertung ?? null,
        rabattart: buchung.rabattart ?? Rabattart.KEIN_RABATT,
        rabattwertTyp: buchung.rabattwertTyp ?? RabattwertTyp.PROZENTUAL,
        rabattwert: buchung.rabattwert ?? 0,
      });

      return;
    }

    setForm((current) => ({
      ...current,
      mietobjektId: mietobjekt?.id ?? current.mietobjektId,
      buchungsquelle: neueBuchungsquelle,
      ...(initialZeitraum
        ? {
            anreise: datumMitZeit(initialZeitraum.anreiseDatum, mietobjekt?.anreisezeit),
            abreise: datumMitZeit(initialZeitraum.abreiseDatum, mietobjekt?.abreisezeit),
          }
        : {}),
    }));
  }, [buchung, mietobjekt?.id, neueBuchungsquelle, initialZeitraum?.anreiseDatum, initialZeitraum?.abreiseDatum]);

  const resetForm = () => {
    if (buchung) {
      const positionen: BuchungPositionSave[] = (buchung.positionen ?? []).map((position) => ({
        mietbereichId: position.mietbereichId,
        anzahl: position.anzahl,
      }));

      setForm({
        anreise: buchung.anreise,
        abreise: buchung.abreise,
        unbefristet: buchung.unbefristet,
        mietobjektId: buchung.mietobjektId,
        mietbereichIds:
          buchung.mietbereichIds?.length > 0
            ? [...buchung.mietbereichIds]
            : positionen.map((position) => position.mietbereichId),
        positionen,
        mieterId: buchung.mieterId,
        veranstalterVereinId: buchung.veranstalterVereinId,
        status: buchung.status,
        buchungsquelle: buchung.buchungsquelle,
        bemerkung: buchung.bemerkung ?? "",
        bewertung: buchung.bewertung ?? null,
        rabattart: buchung.rabattart ?? Rabattart.KEIN_RABATT,
        rabattwertTyp: buchung.rabattwertTyp ?? RabattwertTyp.PROZENTUAL,
        rabattwert: buchung.rabattwert ?? 0,
      });

      return;
    }

    setForm({
      ...createInitialForm(neueBuchungsquelle),
      mietobjektId: mietobjekt?.id ?? 0,
    });
  };

  useEffect(() => {
    if (!buchung?.mieterId) {
      setMieter(undefined);
      return;
    }

    let cancelled = false;

    const loadMieter = async () => {
      try {
        const person = await getPersonById(buchung.mieterId);

        if (cancelled) return;

        setMieter({
          id: person.id,
          name: person.name,
          vorname: person.vorname,
        });
      } catch (error) {
        console.error("Fehler beim Laden des Mieters:", error);
        if (!cancelled) setMieter(undefined);
      }
    };

    void loadMieter();

    return () => {
      cancelled = true;
    };
  }, [buchung?.id, buchung?.mieterId]);

  useEffect(() => {
    const mietobjektId = buchung?.mietobjektId ?? mietobjekt?.id;

    if (!mietobjektId) {
      setMietbereiche([]);
      setRabatteByMietbereich({});
      return;
    }

    const loadMietbereiche = async () => {
      try {
        const data = await getMietbereiche(mietobjektId);
        const mietbareBereiche = data.filter((mietbereich) => mietbereich.mietbar);
        setMietbereiche(mietbareBereiche);
        const rabatte = await Promise.all(mietbareBereiche.map(async (bereich) => {
          try {
            return [bereich.id, await getMietbereichRabatte(mietobjektId, bereich.id)] as const;
          } catch (error) {
            console.error(`Rabatte für Mietbereich ${bereich.id} konnten nicht geladen werden:`, error);
            return [bereich.id, []] as const;
          }
        }));
        setRabatteByMietbereich(Object.fromEntries(rabatte));
      } catch (error) {
        console.error("Fehler beim Laden der Mietbereiche:", error);
        setMietbereiche([]);
        setRabatteByMietbereich({});
      }
    };

    void loadMietbereiche();
  }, [buchung?.mietobjektId, buchung?.positionen, mietobjekt?.id]);

  useEffect(() => {
    // Bei bestehenden Buchungen gespeicherte Positionen erhalten.
    if (buchung) return;

    const erlaubteMietbereiche = mietbereiche.filter((mietbereich) =>
      form.buchungsquelle === "DIREKT" ? mietbereich.direktbuchungAktiv : mietbereich.airbnbAktiv,
    );

    const erlaubteIds = new Set(erlaubteMietbereiche.map((mietbereich) => mietbereich.id));

    setForm((current) => {
      const positionen = current.positionen.filter((position) =>
        erlaubteIds.has(position.mietbereichId),
      );

      if (positionen.length === current.positionen.length) {
        return current;
      }

      return {
        ...current,
        positionen,
        mietbereichIds: positionen.map((position) => position.mietbereichId),
      };
    });
  }, [buchung, mietbereiche, form.buchungsquelle]);

  const update = <K extends keyof BuchungSave>(key: K, value: BuchungSave[K]) => {
    setForm((current) => {
      const updated = {
        ...current,
        [key]: value,
      };

      if (key === "mietbereichIds") {
        const ids = value as number[];

        updated.positionen = ids.map((id) => {
          const existing = current.positionen.find((position) => position.mietbereichId === id);

          return existing ?? { mietbereichId: id, anzahl: 1 };
        });
      }

      if (key === "positionen") {
        const positionen = value as BuchungPositionSave[];
        updated.mietbereichIds = positionen.map((position) => position.mietbereichId);
      }

      return updated;
    });
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
    if (!form.anreise) return null;

    if (!form.unbefristet) {
      if (!form.abreise) return null;
      if (form.abreise < form.anreise) return null;
    }

    return {
      ...form,
      mietbereichIds: form.positionen.map((position) => position.mietbereichId),
    };
  };

  const gefilterteMietbereiche = mietbereiche.filter((mietbereich) =>
    form.buchungsquelle === "DIREKT" ? mietbereich.direktbuchungAktiv : mietbereich.airbnbAktiv,
  );

return {
  form,
  update,
  resetForm,
  mietbereiche: gefilterteMietbereiche,
  rabatteByMietbereich,
  mieter,
  veranstalter,
  setMieter: handleMieterChange,
  setVeranstalter: handleVeranstalterChange,
  buildSavePayload,
};
}
