// api/types/Mitglied.ts
import { VereinRef } from "./VereinRef";

export interface Mitglied {
  id: number;
  verein: VereinRef;
  hauptVerein: boolean;
}
