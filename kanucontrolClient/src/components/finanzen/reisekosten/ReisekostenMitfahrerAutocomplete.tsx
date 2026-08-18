import { EntityAutocomplete } from "@/components/common/reference/EntityAutocomplete";
import { PersonRef } from "@/api/types/person/PersonRef";
import { useCallback } from "react";

import { searchVerfuegbareMitfahrer } from "@/api/services/reisekostenApi";

interface Props {
  veranstaltungId: number;
  value?: PersonRef;
  disabled?: boolean;
  label?: string;
  excludeIds?: number[];
  onChange: (value?: PersonRef) => void;
}

export function ReisekostenMitfahrerAutocomplete({
  veranstaltungId,
  value,
  disabled,
  label = "Person",
  excludeIds,
  onChange,
}: Props) {


  const fetchMitfahrer = useCallback(
    async (search: { search?: string }) => {

      const personen = await searchVerfuegbareMitfahrer(veranstaltungId, search);

     return personen
       .filter((p) => !excludeIds?.includes(p.id))
       .sort(
         (a, b) =>
           a.name.localeCompare(b.name, "de", { sensitivity: "base" }) ||
           a.vorname.localeCompare(b.vorname, "de", { sensitivity: "base" }),
       );
    },
    [veranstaltungId, excludeIds],
  );

  return (
    <EntityAutocomplete<PersonRef>
      label={label}
      value={value}
      disabled={disabled}
      fetch={fetchMitfahrer}
      getLabel={(p) => `${p.name}, ${p.vorname}${p.hauptvereinAbk ? ` (${p.hauptvereinAbk})` : ""}`}
      onChange={onChange}
    />
  );
}
