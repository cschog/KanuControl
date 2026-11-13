import type { BuchungSave } from "@/vermietung/types/BuchungSave";
export const buchungBereiche = ['mieter', 'aufenthalt', 'miete', 'zahlung', 'verbrauch', 'schaeden', 'intern'] as const;
export type BuchungBereich = typeof buchungBereiche[number];
export type BereichFehler = Partial<Record<BuchungBereich, string>>;
/** Speicherregeln; Verbrauch und Kaution prüft weiterhin der Abschlussdialog. */
export function buchungSpeicherFehler(form: BuchungSave): BereichFehler {
  const fehler: BereichFehler = {};
  if (!form.mieterId) fehler.mieter = 'Bitte einen Mieter auswählen.';
  if (!form.anreise || (!form.unbefristet && (!form.abreise || form.abreise <= form.anreise))) {
    fehler.aufenthalt = 'Bitte Beginn und ein zeitlich späteres Ende mit Uhrzeit angeben.';
  }
  if ([form.anzahlJugendliche, form.anzahlErwachsene].some(v => v != null && (!Number.isInteger(v) || v < 0)) ||
    ((form.anzahlJugendliche ?? 0) > 0 &&
      (form.jugendAlterVon == null || form.jugendAlterBis == null || !Number.isInteger(form.jugendAlterVon) || !Number.isInteger(form.jugendAlterBis) || form.jugendAlterVon < 0 || form.jugendAlterBis > 120 || form.jugendAlterVon > form.jugendAlterBis))) {
    fehler.aufenthalt = 'Bitte Teilnehmerzahlen und Altersspanne der Jugendlichen prüfen.';
  }
  if (form.mietpreiseNeuBerechnen && !form.mietpreisStichtag) fehler.miete = 'Bitte einen Preisstand wählen.';
  return fehler;
}
