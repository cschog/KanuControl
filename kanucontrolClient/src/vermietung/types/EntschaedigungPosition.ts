export interface EntschaedigungPosition {
 id?: number; katalogId: number | null; bezeichnung: string; anlass: "BESCHAEDIGUNG" | "VERLUST";
 anzahl: number; einzelpreis: number; katalogpreis: number | null; gesamtbetrag?: number;
 bemerkung: string | null; preisbegruendung: string | null; sortierung: number;
}
