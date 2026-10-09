// src/core/components/person/PersonAutocomplete.tsx
import { EntityAutocomplete } from "@/core/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/core/api/types/person/PersonRef";
import { searchPersons } from "@/core/api/services/personApi";
import type { FieldStatus } from "@/core/components/common/FormFeld";

interface Props {
  value?: PersonRef;
  disabled?: boolean;
  label?: string;
  onChange: (value?: PersonRef) => void;
  nurLeiter?: boolean;
  stichtag?: string;

  dataStatus?: FieldStatus;
  dataStatusMessage?: string;
}

export function PersonAutocomplete({
  value,
  disabled,
  label = "Person",
  onChange,
  nurLeiter = false,
  stichtag,
  dataStatus,
  dataStatusMessage,
}: Props) {
  return (
    <EntityAutocomplete<PersonRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={(params) =>
        searchPersons({
          ...params,
          nurLeiter,
          stichtag,
        })
      }
      getLabel={(p) => `${p.name}, ${p.vorname}${p.hauptvereinAbk ? ` (${p.hauptvereinAbk})` : ""}`}
      onChange={onChange}
      dataStatus={dataStatus}
      dataStatusMessage={dataStatusMessage}
    />
  );
}
