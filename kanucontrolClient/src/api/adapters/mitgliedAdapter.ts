// api/adapters/mitgliedAdapter.ts
import { MitgliedDTO } from "@/api/types/MitgliedDTO";
import { Mitglied } from "@/api/types/Mitglied";

export function mapMitglied(dto: MitgliedDTO): Mitglied {
  if (!dto.id || !dto.vereinId || !dto.vereinName) {
    throw new Error("Ungültiges MitgliedDTO vom Server");
  }

  return {
    id: dto.id,
    verein: {
      id: dto.vereinId,
      name: dto.vereinName,
      abk: dto.vereinAbk ?? undefined,
    },
    hauptVerein: dto.hauptVerein ?? false,
    funktion: dto.funktion ?? undefined,
  };
}
