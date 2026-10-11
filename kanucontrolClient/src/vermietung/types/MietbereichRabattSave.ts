import type { Rabattart } from "@/vermietung/enums/Rabattart";
import type { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

export interface MietbereichRabattSave {
  rabattart: Rabattart;
  rabattwertTyp: RabattwertTyp;
  rabattwert: number;
  aktiv: boolean;
}

