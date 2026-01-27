import { useState } from "react";
import { Person } from "@/api/types/Person";
import { VereinRef } from "@/api/types/VereinRef";
import { Sex } from "@/api/enums/Sex";
import { CountrySelectFeld } from "@/components/common/CountrySelect";
import { FormFeldDate } from "@/components/common/FormFeldDate";
import { EntityEditForm } from "@/components/common/EntityEditForm";
import { FormGrid } from "@/components/common/FormGrid";
import { TextFormFeld } from "@/components/common/TextFormFeld";
import { EnumSelectFeld } from "@/components/common/EnumSelectFeld";
import { Autocomplete, Button, Divider, Stack, TextField, Chip } from "@mui/material";
import { mapMitglied } from "@/api/adapters/mitgliedAdapter";
import axios from "axios";
import { Mitglied } from "@/api/types/Mitglied";
import { MitgliedDTO } from "@/api/types/MitgliedDTO";
import { ApiError } from "@/api/types/ApiError";

interface PersonEditFormProps {
  person: Person;
  vereine?: VereinRef[];
  onSave: (person: Person) => Promise<void>;
  onCreateMitglied?: (personId: number, vereinId: number) => Promise<MitgliedDTO>;
  onCancel: () => void;
}

export function PersonEditForm({
  person,
  vereine,
  onSave,
  onCreateMitglied,
  onCancel,
}: PersonEditFormProps) {

  const [state, setState] = useState<Person>(() => ({
    ...person,
    mitgliedschaften: person.mitgliedschaften ?? [],
    telefon: person.telefon ?? "",
    iban: person.iban ?? "",
    strasse: person.strasse ?? "",
  }));

  const [selectedVerein, setSelectedVerein] = useState<VereinRef | null>(null);
  const [loadingMitglied, setLoadingMitglied] = useState(false);

  const mitglieder: Mitglied[] = state.mitgliedschaften ?? [];

  // 🔑 bereits zugeordnete Vereine
  const zugeordneteVereinIds = new Set(
    mitglieder.map((m) => m.verein?.id).filter((id): id is number => id !== undefined),
  );

  const verfügbareVereine = (vereine ?? []).filter(
    (v) => v.id !== undefined && !zugeordneteVereinIds.has(v.id),
  );

  if (!person || !person.id) {
    return null;
  }

  return (
    <EntityEditForm
      title="Person bearbeiten"
      onCancel={onCancel}
      onSave={async () => {
        await onSave(state); // 🔑 state IST Person
        return true;
      }}
    >
      {/* ========================= PERSON ========================= */}
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
          options={["M", "W", "D"]}
          onChange={(v) => setState((s) => ({ ...s, sex: v }))}
        />

        <FormFeldDate
          label="Geburtsdatum"
          value={state.geburtsdatum ?? ""} // ✅ HIER
          onChange={(v) =>
            setState((s) => ({
              ...s,
              geburtsdatum: v || undefined, // 🔑 "" → undefined zurück
            }))
          }
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
        <CountrySelectFeld
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
      </FormGrid>

      {/* ========================= MITGLIEDSCHAFTEN ========================= */}
      <Divider sx={{ my: 3 }} />

      <Stack spacing={1}>
        {mitglieder?.map((m) => (
          <Chip key={m.id} label={`${m.verein.name}${m.hauptVerein ? " ⭐" : ""}`} size="small" />
        ))}
      </Stack>

      {/* ========================= VEREIN ZUORDNEN ========================= */}
      <Stack spacing={2} sx={{ mt: 2 }}>
        <strong>Verein zuordnen</strong>

        <Autocomplete
          options={verfügbareVereine}
          value={selectedVerein}
          getOptionLabel={(v) => v.name}
          isOptionEqualToValue={(a, b) => a.id === b.id}
          onChange={(_, v) => setSelectedVerein(v)}
          renderInput={(params) => <TextField {...params} label="Verein auswählen" />}
          disabled={verfügbareVereine.length === 0}
        />

        {verfügbareVereine.length === 0 && <em>Alle Vereine sind bereits zugeordnet</em>}

        <Button
          variant="contained"
          disabled={
            !onCreateMitglied || // 🔒 Feature nicht aktiv
            !selectedVerein ||
            loadingMitglied ||
            !state.id
          }
          onClick={async () => {
            if (!onCreateMitglied || !selectedVerein || !state.id) return;

            setLoadingMitglied(true);

            try {
              const dto = await onCreateMitglied(state.id, selectedVerein.id);

              const created = mapMitglied(dto);

              setState((s) => ({
                ...s,
                mitgliedschaften: [
                  ...(s.mitgliedschaften ?? []).map((m) => ({
                    ...m,
                    hauptVerein: false, // 🔑 alle alten zurücksetzen
                  })),
                  created, // 🔑 neuer ist Hauptverein (kommt so vom Backend)
                ],
              }));

              setSelectedVerein(null);
            } catch (err: unknown) {
              if (axios.isAxiosError(err)) {
                const status = err.response?.status;
                const data = err.response?.data as ApiError | undefined;

                if (status === 409) {
                  alert("Diese Mitgliedschaft existiert bereits");
                  return;
                }

                if (data?.message) {
                  alert(data.message);
                  return;
                }

                alert(`Serverfehler (${status ?? "kein Status"})`);
              } else {
                alert("Unerwarteter Fehler");
              }
            } finally {
              setLoadingMitglied(false);
            }
          }}
        >
          Verein zuordnen
        </Button>
      </Stack>
    </EntityEditForm>
  );
}
