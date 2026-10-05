import { WithId } from "@/core/components/common/GenericTableTanstack";
export interface KostenRow extends WithId {
  datum: string;
  person: string;
  kategorie: string;
  kommentar: string;
  einnahme?: number;
  ausgabe?: number;
}
