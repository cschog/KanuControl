import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { VereinRef } from "@/api/types/VereinRef";
import { searchVereine } from "@/api/services/vereinApi";

interface Props {
  value?: VereinRef;
  disabled?: boolean;
  onChange: (value?: VereinRef) => void;
}

export function VereinAutocomplete({ value, disabled, onChange }: Props) {
  return (
    <EntityAutocomplete<VereinRef>
      label="Verein"
      value={value}
      disabled={disabled}
      fetch={searchVereine}
      getLabel={(v) => v.name}
      onChange={onChange}
    />
  );
}
