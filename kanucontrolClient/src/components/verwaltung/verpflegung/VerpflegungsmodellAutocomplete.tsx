import { RefAutocomplete } from "@/components/common/RefAutocomplete";
import { useLoad } from "@/hooks/useLoad";

import { VerpflegungsmodellRef } from "@/api/types/veranstaltung/VerpflegungsmodellRef";
import { getVerpflegungsmodellRefs } from "@/api/services/verpflegungsmodellApi";

interface Props {
  value?: VerpflegungsmodellRef;
  onChange: (value?: VerpflegungsmodellRef) => void;
  disabled?: boolean;
}

export function VerpflegungsmodellAutocomplete({
  value,
  onChange,
  disabled,
}: Props) {
  const modelle = useLoad(getVerpflegungsmodellRefs, {
    initialData: [],
  });

  return (
    <RefAutocomplete
      label="Verpflegungsmodell"
      options={modelle.data}
      loading={modelle.loading}
      value={value}
      disabled={disabled}
      onChange={(v) => onChange(v ?? undefined)}
    />
  );
}