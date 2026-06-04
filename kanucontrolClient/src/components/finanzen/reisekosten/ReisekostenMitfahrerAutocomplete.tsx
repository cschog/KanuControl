import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/PersonRef";

import { searchVerfuegbareMitfahrer } from "@/api/services/reisekostenApi";

interface Props {
  veranstaltungId: number;
  value?: PersonRef;
  disabled?: boolean;
  label?: string;
  excludeIds?: number[];
  onChange: (value?: PersonRef) => void;
}

export function ReisekostenMitfahrerAutocomplete({
  veranstaltungId,
  value,
  disabled,
  label = "Person",
  excludeIds,
  onChange,
}: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={async (search) => {
        const personen = await searchVerfuegbareMitfahrer(veranstaltungId, search);
        return personen.filter((p) => !excludeIds?.includes(p.id));
      }}
      getLabel={(p) => `${p.name}, ${p.vorname}${p.hauptvereinAbk ? ` (${p.hauptvereinAbk})` : ""}`}
      onChange={onChange}
    />
  );
}
