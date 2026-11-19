import type { Verbrauchskosten } from "../types/Verbrauchskosten";

export interface VerbrauchspreisAuswahl {
  konfiguration: Verbrauchskosten;
  preis: number | null;
  hinweis: string | null;
}

/** Entspricht der serverseitigen Auswahl: genau ein Tarif je Verbrauchsart am Anreisetag. */
export function waehleVerbrauchspreise(positionen: Verbrauchskosten[], datum?: string): VerbrauchspreisAuswahl[] {
  const gruppen = new Map<Verbrauchskosten["verbrauchsart"], Verbrauchskosten[]>();
  for (const position of positionen) {
    const gruppe = gruppen.get(position.verbrauchsart) ?? [];
    gruppe.push(position);
    gruppen.set(position.verbrauchsart, gruppe);
  }
  return [...gruppen.entries()].map(([art, gruppe]) => {
    if (!datum) return { konfiguration: gruppe[0], preis: null, hinweis: "Bitte das Anreisedatum erfassen." };
    const passende = gruppe.flatMap(konfiguration => (konfiguration.preise ?? [])
      .filter(p => p.gueltigVon <= datum && (!p.gueltigBis || datum <= p.gueltigBis))
      .map(p => ({ konfiguration, preis: p.preis, hinweis: null })));
    if (passende.length === 1) return passende[0];
    const name = { STROM: "Strom", GAS: "Gas", WASSER: "Wasser" }[art];
    const tag = datum.split("-").reverse().join(".");
    return {
      konfiguration: gruppe[0], preis: null,
      hinweis: passende.length === 0 ? `Kein gültiger Preis für ${name} am Anreisetag ${tag}. Bitte den Gültigkeitszeitraum ergänzen.`
        : `Mehrere gültige Preise für ${name} am Anreisetag ${tag}. Bitte überschneidende Zeiträume korrigieren.`,
    };
  });
}
