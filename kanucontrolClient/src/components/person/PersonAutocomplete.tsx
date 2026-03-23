import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/PersonRef";
import { searchPersons } from "@/api/services/personApi";

interface Props {
  value?: PersonRef;
  disabled?: boolean;
  label?: string; // ⭐ HINZUFÜGEN
  onChange: (value?: PersonRef) => void;
}

export function PersonAutocomplete({ value, disabled, onChange }: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label="Leitung"
      value={value}
      disabled={disabled}
      fetch={searchPersons}
      getLabel={(p) => `${p.name}, ${p.vorname}`}
      onChange={onChange}
    />
  );
}
