import { useState } from "react";
import { Person } from "@/api/types/Person";
import { Sex } from "@/api/enums/Sex";
import { FormFeldDate } from "@/components/common/FormFeldDate";
import { EntityEditForm } from "@/components/common/EntityEditForm";
import { FormGrid } from "@/components/common/FormGrid";
import { TextFormFeld } from "@/components/common/TextFormFeld";
import { BooleanFormFeld } from "@/components/common/BooleanFormFeld";
import { EnumSelectFeld } from "@/components/common/EnumSelectFeld";

interface PersonEditFormProps {
  person: Person;
  onSave: (person: Person) => Promise<void>;
  onCancel: () => void;
}

export function PersonEditForm({
  person,
  onSave,
  onCancel,
}: PersonEditFormProps) {
  const [state, setState] = useState<Person>(person);

  return (
    <EntityEditForm
      title="Mitglied bearbeiten"
      onCancel={onCancel}
      onSave={async () => {
        await onSave(state);
        return true;
      }}
    >
      <FormGrid>
        <TextFormFeld
          label="Name"
          value={state.name}
          onChange={(v) => setState((s) => ({ ...s, name: v }))}
        />

        <TextFormFeld
          label="Vorname"
          value={state.vorname}
          onChange={(v) => setState((s) => ({ ...s, vorname: v }))}
        />

        <EnumSelectFeld<Sex>
          label="Geschlecht"
          value={state.sex}
          options={["MAENNLICH", "WEIBLICH", "DIVERS"]}
          onChange={(v) => setState((s) => ({ ...s, sex: v }))}
        />

        <FormFeldDate
          label="Geburtsdatum"
          value={state.geburtsdatum}
          onChange={(v) => setState((s) => ({ ...s, geburtsdatum: v }))}
        />
        <TextFormFeld
          label="Straße"
          value={state.strasse}
          onChange={(v) => setState((s) => ({ ...s, strasse: v }))}
        />

        <TextFormFeld
          label="PLZ"
          value={state.plz}
          onChange={(v) => setState((s) => ({ ...s, plz: v }))}
        />

        <TextFormFeld
          label="Ort"
          value={state.ort}
          onChange={(v) => setState((s) => ({ ...s, ort: v }))}
        />

        <TextFormFeld
          label="Land"
          value={state.countryCode}
          onChange={(v) => setState((s) => ({ ...s, countryCode: v }))}
        />

        <TextFormFeld
          label="Telefon"
          value={state.telefon}
          onChange={(v) => setState((s) => ({ ...s, telefon: v }))}
        />

        <TextFormFeld
          label="Festnetz"
          value={state.telefonFestnetz}
          onChange={(v) => setState((s) => ({ ...s, telefonFestnetz: v }))}
        />

        <TextFormFeld
          label="Bank"
          value={state.bankName}
          onChange={(v) => setState((s) => ({ ...s, bankName: v }))}
        />

        <TextFormFeld
          label="IBAN"
          value={state.iban}
          onChange={(v) => setState((s) => ({ ...s, iban: v }))}
        />

        <BooleanFormFeld
          label="Aktiv"
          value={state.aktiv}
          onChange={(v) => setState((s) => ({ ...s, aktiv: v }))}
        />
      </FormGrid>
    </EntityEditForm>
  );
}