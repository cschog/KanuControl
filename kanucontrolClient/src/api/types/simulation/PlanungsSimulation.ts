import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";

export interface Beitragsstruktur {
    id: number;
    name: string;
}

export interface PlanungsSimulation {

    typ: VeranstaltungTyp;

    teilnehmer: number;
    mitarbeiter: number;

    tage: number;
    naechte: number;

    standardGebuehr?: number;

    unterkunftPreisProPersonUndNacht?: number;
    verpflegungPreisProPersonUndTag?: number;

    kikZertifiziert: boolean;

    beginnDatum?: string;

    beitragsstruktur?: Beitragsstruktur;

}