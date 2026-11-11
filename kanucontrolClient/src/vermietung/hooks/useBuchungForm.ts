import { useEffect, useState } from "react";

import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { VereinRef } from "@/core/api/types/verein/VereinRef";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave, BuchungPositionSave } from "@/vermietung/types/BuchungSave";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";
import { getMietbereichRabatte } from "@/vermietung/api/mietbereichRabattApi";
import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

const createInitialForm = (buchungsquelle: Buchungsquelle = "DIREKT"): BuchungSave => ({
  bankverbindungAuswahl: "PRIMAER",
  anreise: "",
  abreise: "",
  unbefristet: false,
  mietobjektId: 0,
  mietbereichIds: [],
  positionen: [],
  entschaedigungen: [],
  mieterId: 0,
  veranstalterVereinId: undefined,
  status: "ANFRAGE",
  buchungsquelle,
  anzahlJugendliche: null,
  jugendAlterVon: null,
  jugendAlterBis: null,
  anzahlErwachsene: null,
  bemerkung: "",
  bewertung: null,
  rabattart: Rabattart.KEIN_RABATT,
  rabattwertTyp: RabattwertTyp.PROZENTUAL,
  rabattwert: 0,
  kautionBetrag: buchungsquelle === "DIREKT" ? 250 : 0,
  anzahlungBetrag: buchungsquelle === "DIREKT" ? 50 : 0,
});

const datumMitZeit = (datum: string, zeit: string | null | undefined): string => {
  const formatierteZeit = !zeit ? "00:00:00" : zeit.length === 5 ? `${zeit}:00` : zeit;
  return datum ? `${datum}T${formatierteZeit}` : "";
};

const mieterAusBuchung = (buchung: Buchung | null): PersonRef | undefined => buchung?.mieterId
  ? { id: buchung.mieterId, name: buchung.mieterName, vorname: buchung.mieterVorname }
  : undefined;

export function useBuchungForm(
  buchung: Buchung | null,
  neueBuchungsquelle: Buchungsquelle = "DIREKT",
  initialZeitraum?: { anreiseDatum: string; abreiseDatum: string },
) {
  const { mietobjekt } = useVermietungContext();
  const initialAnreiseDatum = initialZeitraum?.anreiseDatum;
  const initialAbreiseDatum = initialZeitraum?.abreiseDatum;

  const [form, setForm] = useState<BuchungSave>(() => createInitialForm(neueBuchungsquelle));

  const [zahlungsvarianteManuell, setZahlungsvarianteManuell] = useState(false);
  const [mieter, setMieter] = useState<PersonRef | undefined>();
  const [veranstalter, setVeranstalter] = useState<VereinRef | undefined>();
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [rabatteByMietbereich, setRabatteByMietbereich] = useState<Record<number, MietbereichRabatt[]>>({});

  useEffect(() => {
    setMieter(mieterAusBuchung(buchung));
    if (buchung) {
      const positionen: BuchungPositionSave[] = (buchung.positionen ?? []).map((position) => ({
        mietbereichId: position.mietbereichId,
        anzahl: position.anzahl,
      }));

      setForm({
        entschaedigungen: buchung.entschaedigungen ?? [],
        abrechnungRevision: buchung.abrechnungRevision,
        zahlungsvariante: buchung.zahlungsvariante ?? "ANZAHLUNG",
        bankverbindungAuswahl: buchung.bankverbindungAuswahl ?? "PRIMAER",
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
        anzahlJugendliche: buchung.anzahlJugendliche ?? null,
        jugendAlterVon: buchung.jugendAlterVon ?? null,
        jugendAlterBis: buchung.jugendAlterBis ?? null,
        anzahlErwachsene: buchung.anzahlErwachsene ?? null,
        bemerkung: buchung.bemerkung ?? "",
        bewertung: buchung.bewertung ?? null,
        rabattart: buchung.buchungsquelle === "AIRBNB" ? Rabattart.KEIN_RABATT : buchung.rabattart ?? Rabattart.KEIN_RABATT,
        rabattwertTyp: buchung.buchungsquelle === "AIRBNB" ? null : buchung.rabattwertTyp ?? RabattwertTyp.PROZENTUAL,
        rabattwert: buchung.buchungsquelle === "AIRBNB" ? null : buchung.rabattwert ?? 0,
        kautionBetrag: buchung.buchungsquelle === "AIRBNB" ? 0 : buchung.kautionBetrag ?? 0,
        anzahlungBetrag: buchung.buchungsquelle === "AIRBNB" ? 0 : buchung.anzahlungBetrag ?? 0,
      });

      return;
    }

    setForm((current) => ({
      ...current,
      mietobjektId: mietobjekt?.id ?? current.mietobjektId,
      buchungsquelle: neueBuchungsquelle,
      ...(neueBuchungsquelle === "AIRBNB"
        ? {
            rabattart: Rabattart.KEIN_RABATT,
            rabattwertTyp: null,
            rabattwert: null,
            kautionBetrag: 0,
            anzahlungBetrag: 0,
          }
        : {}),
      ...(initialAnreiseDatum !== undefined && initialAbreiseDatum !== undefined
        ? {
            anreise: datumMitZeit(initialAnreiseDatum, mietobjekt?.anreisezeit),
            abreise: datumMitZeit(initialAbreiseDatum, mietobjekt?.abreisezeit),
          }
        : {}),
    }));
  }, [buchung, mietobjekt?.id, mietobjekt?.anreisezeit, mietobjekt?.abreisezeit, neueBuchungsquelle, initialAnreiseDatum, initialAbreiseDatum]);

  const resetForm = () => {
    setZahlungsvarianteManuell(false);
    setMieter(mieterAusBuchung(buchung));
    if (buchung) {
      const positionen: BuchungPositionSave[] = (buchung.positionen ?? []).map((position) => ({
        mietbereichId: position.mietbereichId,
        anzahl: position.anzahl,
      }));

      setForm({
        entschaedigungen: buchung.entschaedigungen ?? [],
        abrechnungRevision: buchung.abrechnungRevision,
        zahlungsvariante: buchung.zahlungsvariante ?? "ANZAHLUNG",
        bankverbindungAuswahl: buchung.bankverbindungAuswahl ?? "PRIMAER",
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
        anzahlJugendliche: buchung.anzahlJugendliche ?? null,
        jugendAlterVon: buchung.jugendAlterVon ?? null,
        jugendAlterBis: buchung.jugendAlterBis ?? null,
        anzahlErwachsene: buchung.anzahlErwachsene ?? null,
        bemerkung: buchung.bemerkung ?? "",
        bewertung: buchung.bewertung ?? null,
        rabattart: buchung.buchungsquelle === "AIRBNB" ? Rabattart.KEIN_RABATT : buchung.rabattart ?? Rabattart.KEIN_RABATT,
        rabattwertTyp: buchung.buchungsquelle === "AIRBNB" ? null : buchung.rabattwertTyp ?? RabattwertTyp.PROZENTUAL,
        rabattwert: buchung.buchungsquelle === "AIRBNB" ? null : buchung.rabattwert ?? 0,
        kautionBetrag: buchung.buchungsquelle === "AIRBNB" ? 0 : buchung.kautionBetrag ?? 0,
        anzahlungBetrag: buchung.buchungsquelle === "AIRBNB" ? 0 : buchung.anzahlungBetrag ?? 0,
      });

      return;
    }

    setForm({
      ...createInitialForm(neueBuchungsquelle),
      mietobjektId: mietobjekt?.id ?? 0,
    });
  };

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

  useEffect(() => {
    if (buchung || zahlungsvarianteManuell || form.buchungsquelle !== "DIREKT" || !form.anreise) return;
    const heute = new Intl.DateTimeFormat("sv-SE", { timeZone: "Europe/Berlin" }).format(new Date());
    const tage = (Date.parse(form.anreise.slice(0, 10)) - Date.parse(heute)) / 86400000;
    const variante = tage < (mietobjekt?.kurzfristgrenzeTage ?? 42) && (mietobjekt?.kurzfristgrenzeTage ?? 42) > 0 ? "GESAMTMIETE_VORAUS" : "ANZAHLUNG";
    setForm(current => current.zahlungsvariante === variante ? current : { ...current, zahlungsvariante: variante });
  }, [buchung, zahlungsvarianteManuell, form.anreise, form.buchungsquelle, mietobjekt?.kurzfristgrenzeTage]);

  const update = <K extends keyof BuchungSave>(key: K, value: BuchungSave[K]) => {
    if (key === "zahlungsvariante") setZahlungsvarianteManuell(true);
    setForm((current) => {
      const updated = {
        ...current,
        [key]: value,
      };

      if (key === "anreise" && updated.anreise && updated.abreise && !updated.unbefristet
          && updated.abreise.slice(0, 10) < updated.anreise.slice(0, 10)) {
        // Beim Verschieben des Beginns ein bereits gewähltes Ende mitziehen.
        // Die Abreiseuhrzeit bleibt erhalten; Tagesbuchungen prüfen beide Uhrzeiten.
        updated.abreise = updated.anreise.slice(0, 10) + updated.abreise.slice(10);
      }

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
      anzahlungBetrag: (form.zahlungsvariante ?? "ANZAHLUNG") === "ANZAHLUNG" ? form.anzahlungBetrag : 0,
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
