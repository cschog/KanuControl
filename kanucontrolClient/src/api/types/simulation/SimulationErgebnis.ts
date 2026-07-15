import { BeitragsVorschlag } from "@/api/types/simulation/BeitragsVorschlag";
import { PlanungspositionDTO } from "./PlanungspositionDTO";

export interface SimulationErgebnis {
    positionen: PlanungspositionDTO[];

    kosten: number;
    einnahmen: number;
    saldo: number;

    summeTeilnehmerbeitraege: number;
    durchschnittlicherPersonenbeitrag: number;
    empfohlenerPersonenbeitrag: number;

    beitragsVorschlag: BeitragsVorschlag;
}