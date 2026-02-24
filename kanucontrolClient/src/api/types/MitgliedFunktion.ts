// api/types/MitgliedFunktion.ts

export type MitgliedFunktion =
  | "VORSITZENDER"
  | "JUGENDWART"
  | "BOOTSHAUSWART"
  | "SPORTWART"
  | "JUGENDDELEGIERTER"
  | "PSG_BEAUFTRAGTE"
  | "WANDERWART"
  | "GESCHAEFTSFUEHRER"
  | "KASSENWART";

  export const MitgliedFunktionLabel: Record<MitgliedFunktion, string> = {
    VORSITZENDER: "Vorsitzender",
    JUGENDWART: "Jugendwart",
    BOOTSHAUSWART: "Bootshauswart",
    SPORTWART: "Sportwart",
    JUGENDDELEGIERTER: "Jugenddelegierter",
    PSG_BEAUFTRAGTE: "PSG-Beauftragte",
    WANDERWART: "Wanderwart",
    GESCHAEFTSFUEHRER: "Geschäftsführer",
    KASSENWART: "Kassenwart",
  };