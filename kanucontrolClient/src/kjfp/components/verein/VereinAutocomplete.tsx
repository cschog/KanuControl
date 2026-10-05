import { EntityAutocomplete } from "@/core/components/common/reference/EntityAutocomplete";
import { VereinRef } from "@/kjfp/types/verein/VereinRef";
import { searchVereine } from "@/kjfp/api/services/vereinApi";

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
