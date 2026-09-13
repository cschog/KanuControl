export interface FinanzausgleichDTO {
  finanzGruppeId: number;
  finanzGruppeKuerzel: string;

  teilnehmerBeitraegeSoll: number;

  teilnehmerBeitraegeUeberweisung: number;
  teilnehmerBeitraegeQuittung: number;

  ausgaben: number;
  fahrkosten: number;

  erstattungVomVK: number;

  beitragsstatus?: "OK" | "ABWEICHUNG";
}

export interface FinanzausgleichRow {
  id: number;

  finanzGruppeId: number;
  kuerzel: string;

  teilnehmerBeitraegeSoll: number;

  teilnehmerBeitraegeUeberweisung: number;
  teilnehmerBeitraegeQuittung: number;

  ausgaben: number;
  fahrkosten: number;

  erstattungVomVK: number;
  beitragsstatus?: "OK" | "ABWEICHUNG";
}
