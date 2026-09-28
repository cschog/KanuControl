import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/person/PersonRef";
import { searchPersons } from "@/api/services/personApi";
import type { FieldStatus } from "@/components/common/FormFeld";

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
