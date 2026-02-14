import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/PersonRef";
import { searchPersonsPage } from "@/api/services/personApi";

interface Props {
  value?: PersonRef;
  disabled?: boolean;
  onChange: (value?: PersonRef) => void;
}

export function PersonAutocomplete({ value, disabled, onChange }: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label="Person"
      value={value}
      disabled={disabled}
      fetch={searchPersonsPage}
      getLabel={(p) => `${p.name}, ${p.vorname}`}
      onChange={onChange}
    />
  );
}
