import type { BankverbindungAuswahl } from "./Bankverbindung";
import type { EntschaedigungPosition } from "./EntschaedigungPosition";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import type { Rabattart } from "@/vermietung/enums/Rabattart";
import type { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

export interface BuchungPositionSave {
  mietbereichId: number;
  anzahl: number;
}

export interface BuchungSave {
  mietpreiseNeuBerechnen?: boolean;
  mietpreisStichtag?: string;
  zahlungsvariante?: "ANZAHLUNG" | "BEI_ABRECHNUNG" | "GESAMTMIETE_VORAUS";
  bankverbindungAuswahl?: BankverbindungAuswahl;
  entschaedigungen?: EntschaedigungPosition[];
  abrechnungRevision?: number;
  abschlussRevision?: number;
  anreise: string;
  abreise: string;

  unbefristet?: boolean;

  mietobjektId: number;
  mieterId: number;

  buchungsquelle: Buchungsquelle;

  mietbereichIds: number[];
  positionen: BuchungPositionSave[];

  veranstalterVereinId?: number;
  status?: Buchungsstatus;
  
  anzahlJugendliche?: number | null;
  jugendAlterVon?: number | null;
  jugendAlterBis?: number | null;
  anzahlErwachsene?: number | null;

  bemerkung: string;
  bewertung: number | null;

  rabattart: Rabattart;
  rabattwertTyp: RabattwertTyp | null;
  rabattwert: number | null;
  kautionBetrag: number;
  anzahlungBetrag: number;
}
