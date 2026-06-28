import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/person/PersonRef";

import { searchVerfuegbareReisekostenPersonen } from "@/api/services/reisekostenApi";

interface Props {
  veranstaltungId: number;

  value?: PersonRef;

  disabled?: boolean;

  label?: string;

  onChange: (value?: PersonRef) => void;
}

export function ReisekostenPersonAutocomplete({
  veranstaltungId,
  value,
  disabled,
  label = "Person",
  onChange,
}: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={(search) => searchVerfuegbareReisekostenPersonen(veranstaltungId, search)}
      getLabel={(p) => `${p.name}, ${p.vorname}${p.hauptvereinAbk ? ` (${p.hauptvereinAbk})` : ""}`}
      onChange={onChange}
    />
  );
}
