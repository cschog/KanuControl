import { VeranstaltungsInfo } from "./VeranstaltungsInfo";

export interface Beitragsstruktur {
    id: number;
    name: string;
}

export interface PlanungsSimulation {

    veranstaltung: VeranstaltungsInfo;

    beitragsstrukturId?: number;

    teilnehmerBeitragUnter21Jahre?: number;

    mitarbeiterBeitrag?: number;

    teilnehmer: number;

    mitarbeiter: number;

    unterkunftPreisProPersonUndNacht?: number;

    verpflegungPreisProPersonUndTag?: number;

    honorare?: number;

    fahrtkosten?: number;

    verbrauchsmaterialProTag?: number;

    sonstigeKostenProTag?: number;

    kultur?: number;

    miete?: number;

    pfand?: number;

    sonstigeEinnahmenProTag?: number;
}