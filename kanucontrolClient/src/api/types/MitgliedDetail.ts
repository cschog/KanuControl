// src/api/types/MitgliedDetail.ts

import { MitgliedFunktion } from "@/api/enums/MitgliedFunktion";
import { VereinRef } from "./VereinRef";

export interface MitgliedDetail {
  id: number;
  hauptVerein: boolean;
  funktion?: MitgliedFunktion;
  verein: VereinRef;
}
