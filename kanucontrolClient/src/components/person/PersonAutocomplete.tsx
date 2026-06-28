import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/person/PersonRef";
import { searchPersons } from "@/api/services/personApi";

interface Props {
  value?: PersonRef;
  disabled?: boolean;
  label?: string;
  onChange: (value?: PersonRef) => void;
}

export function PersonAutocomplete({ value, disabled, label = "Person", onChange }: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={searchPersons}
      getLabel={(p) => `${p.name}, ${p.vorname}${p.hauptvereinAbk ? ` (${p.hauptvereinAbk})` : ""}`}
      onChange={onChange}
    />
  );
}
