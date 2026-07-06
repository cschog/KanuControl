// api/types/simulation/PlanungsSimulation.ts
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";

export interface Beitragsstruktur {
    id: number;
    name: string;
}

export interface PlanungsSimulation {

    typ: VeranstaltungTyp;

    beginnDatum?: string;
    kikZertifiziert: boolean;

    beitragsstrukturId?: number;
    teilnehmerBeitragUnter21Jahre?: number;
    mitarbeiterBeitrag?: number;

    teilnehmer: number;
    mitarbeiter: number;

    tage: number;
    naechte: number;

    unterkunftPreisProPersonUndNacht?: number;
    verpflegungPreisProPersonUndTag?: number;

    honorare?: number;
    fahrtkosten?: number;
    verbrauchsmaterialProTag?: number;
    kultur?: number;
    miete?: number;
    sonstigeKostenProTag?: number;

    pfand?: number;
    sonstigeEinnahmenProTag?: number;
}