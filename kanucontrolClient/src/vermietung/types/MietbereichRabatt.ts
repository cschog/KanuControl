import type { Rabattart } from "@/vermietung/enums/Rabattart";
import type { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

export interface MietbereichRabatt {
  id: number;
  mietbereichId: number;
  rabattart: Rabattart;
  rabattwertTyp: RabattwertTyp;
  rabattwert: number;
  aktiv: boolean;
}
