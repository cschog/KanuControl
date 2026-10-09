// src/core/components/verein/VereinAutocomplete.tsx
import { EntityAutocomplete } from "@/core/components/common/reference/EntityAutocomplete";
import { VereinRef } from "@/core/api/types/verein/VereinRef";
import { searchVereine } from "@/core/api/services/vereinApi";

interface Props {
  label?: string;
  value?: VereinRef;
  disabled?: boolean;
  onChange: (value?: VereinRef) => void;
}

export function VereinAutocomplete({ label = "Verein", value, disabled, onChange }: Props) {
  return (
    <EntityAutocomplete<VereinRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={searchVereine}
      getLabel={(v) => v.name}
      onChange={onChange}
    />
  );
}
