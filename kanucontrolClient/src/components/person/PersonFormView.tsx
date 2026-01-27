import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Button,
  TextField,
  Card,
  CardHeader,
  CardContent,
  MenuItem,
  Stack,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";

import { BottomActionBar } from "@/components/common/BottomActionBar";
import api from "@/api/client/apiClient";

import { PersonDetail } from "@/api/types/PersonDetail";
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { normalizeGermanDate } from "@/utils/dateUtils";

import { getPersonById } from "@/api/services/personApi";
import { createMitglied } from "@/api/services/mitgliedServices";

/* =========================================================
   Types
   ========================================================= */

type PersonDraft = Partial<PersonDetail>;

const str = (v?: string | null) => v ?? undefined;

interface VereinRef {
  id: number;
  name: string;
  abk?: string;
}

interface PersonFormViewProps {
  selectedPerson: PersonDetail | null;

  onNeuePerson: () => void;
  onÄndernPerson: (person: PersonDetail) => Promise<void>;
  onDeletePerson: () => void;
  onStartMenue: () => void;

  btnÄndernPerson: boolean;
  btnLöschenPerson: boolean;
}

/* =========================================================
   Reusable Field
   ========================================================= */

const Field: React.FC<{
  label: string;
  value?: string;
  editMode: boolean;
  onChange?: (v: string) => void;
}> = ({ label, value, editMode, onChange }) => (
  <Box
    sx={{
      p: 0.75,
      borderRadius: 1,
      bgcolor: editMode ? "rgba(194,24,91,0.08)" : "grey.50",
    }}
  >
    <Typography variant="caption" color="text.secondary">
      {label}
    </Typography>

    {editMode ? (
      <TextField
        value={value ?? ""}
        variant="standard"
        size="small"
        fullWidth
        onChange={(e) => onChange?.(e.target.value)}
        InputProps={{ disableUnderline: true }}
        sx={{ mt: 0.25, fontSize: "0.875rem" }}
      />
    ) : (
      <Typography variant="body2" sx={{ fontWeight: 500 }}>
        {value || "–"}
      </Typography>
    )}
  </Box>
);

/* =========================================================
   Component
   ========================================================= */

export const PersonFormView: React.FC<PersonFormViewProps> = ({
  selectedPerson,
  onNeuePerson,
  onÄndernPerson,
  onDeletePerson,
  onStartMenue,
  btnÄndernPerson,
  btnLöschenPerson,
}) => {
  const [editMode, setEditMode] = useState(false);
  const [draft, setDraft] = useState<PersonDraft>({});
  const [confirmOpen, setConfirmOpen] = useState(false);

  // Vereins-Dialog
  const [addVereinOpen, setAddVereinOpen] = useState(false);
  const [vereine, setVereine] = useState<VereinRef[]>([]);
  const [selectedVereinId, setSelectedVereinId] = useState<number | "">("");


  /* ---------------- Load Vereine ---------------- */

  useEffect(() => {
    api.get<VereinRef[]>("/verein").then((res) => setVereine(res.data));
  }, []);

  /* ---------------- Sync selectedPerson → draft ---------------- */

 useEffect(() => {
   if (!selectedPerson) {
     setDraft({});
     setEditMode(false);
     return;
   }

  setDraft({ ...selectedPerson });
   setEditMode(!selectedPerson.id);
 }, [selectedPerson]);

  const mitgliedschaften = draft.mitgliedschaften ?? [];
  const zugeordneteIds = new Set(mitgliedschaften.map((m) => m.verein.id));
  const verfügbareVereine = vereine.filter((v) => !zugeordneteIds.has(v.id));

  const normalizedDate = draft.geburtsdatum ? normalizeGermanDate(draft.geburtsdatum) : undefined;

  const update = <K extends keyof PersonDraft>(key: K, value: PersonDraft[K]) =>
    setDraft((p) => ({ ...p, [key]: value }));


  /* ---------------- Mitglied Actions ---------------- */

 const reloadSelectedPerson = async () => {
   if (!selectedPerson?.id) return;

   const fresh = await getPersonById(selectedPerson.id);
  setDraft(fresh);
 };

  const deleteMitglied = async (mitgliedId: number) => {
    await api.delete(`/mitglied/${mitgliedId}`);
    await reloadSelectedPerson();
  };

  const setHauptverein = async (mitgliedId: number) => {
    await api.put(`/mitglied/${mitgliedId}/hauptverein`);
    await reloadSelectedPerson();
  };

  /* =========================================================
     Render
     ========================================================= */

  if (!selectedPerson) {
    return (
      <>
        <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
          Bitte wählen Sie eine Person aus.
        </Typography>

        <BottomActionBar
          left={[
            { label: "Neue Person", onClick: onNeuePerson },
            { label: "Zurück", onClick: onStartMenue },
          ]}
        />
      </>
    );
  }

  return (
    <>
      {/* ================= DETAILS ================= */}

      <Box
        display="grid"
        gridTemplateColumns={{ xs: "1fr", sm: "repeat(2,1fr)", md: "repeat(3,1fr)" }}
        gap={1.25}
      >
        <Field
          label="Name"
          value={draft.name}
          editMode={editMode}
          onChange={(v) => update("name", v)}
        />
        <Field
          label="Vorname"
          value={draft.vorname}
          editMode={editMode}
          onChange={(v) => update("vorname", v)}
        />

        <Field
          label="Geschlecht"
          value={draft.sex}
          editMode={editMode}
          onChange={(v) => update("sex", v as Sex)}
        />

        <Field
          label="Geburtsdatum"
          value={draft.geburtsdatum}
          editMode={editMode}
          onChange={(v) => update("geburtsdatum", v)}
        />

        <Field
          label="Straße"
          value={draft.strasse}
          editMode={editMode}
          onChange={(v) => update("strasse", v)}
        />
        <Field
          label="PLZ"
          value={draft.plz}
          editMode={editMode}
          onChange={(v) => update("plz", v)}
        />
        <Field
          label="Ort"
          value={draft.ort}
          editMode={editMode}
          onChange={(v) => update("ort", v)}
        />

        <Field
          label="Land"
          value={draft.countryCode}
          editMode={editMode}
          onChange={(v) => update("countryCode", v as CountryCode)}
        />

        <Field
          label="Telefon"
          value={str(draft.telefon)}
          editMode={editMode}
          onChange={(v) => update("telefon", v)}
        />

        <Field
          label="Festnetz"
          value={draft.telefonFestnetz}
          editMode={editMode}
          onChange={(v) => update("telefonFestnetz", v)}
        />
        <Field
          label="Bank"
          value={draft.bankName}
          editMode={editMode}
          onChange={(v) => update("bankName", v)}
        />
        <Field
          label="IBAN"
          value={draft.iban}
          editMode={editMode}
          onChange={(v) => update("iban", v)}
        />
      </Box>

      {/* ================= VEREINE ================= */}

      {mitgliedschaften.length > 0 && (
        <Card sx={{ mt: 3 }}>
          <CardHeader title="Vereine" />
          <CardContent>
            <Stack spacing={1}>
              {mitgliedschaften.map((m) => (
                <Box
                  key={m.id}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    px: 1,
                    py: 0.5,
                    borderRadius: 1,
                    bgcolor: m.hauptVerein ? "action.selected" : "transparent",
                  }}
                >
                  <Typography>
                    {m.verein.name}
                    {m.verein.abk && ` (${m.verein.abk})`}
                  </Typography>

                  <Stack direction="row" spacing={1}>
                    {editMode && !m.hauptVerein && (
                      <Button size="small" onClick={() => setHauptverein(m.id)}>
                        Als Hauptverein setzen
                      </Button>
                    )}
                    {editMode && (
                      <Button size="small" color="error" onClick={() => deleteMitglied(m.id)}>
                        Entfernen
                      </Button>
                    )}
                    {!editMode && m.hauptVerein && (
                      <Chip size="small" label="Hauptverein" color="primary" />
                    )}
                  </Stack>
                </Box>
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      {editMode && (
        <Button size="small" sx={{ mt: 1 }} onClick={() => setAddVereinOpen(true)}>
          Verein zuordnen
        </Button>
      )}

      {/* ================= ACTION BAR ================= */}

      <BottomActionBar
        left={
          editMode
            ? [
                {
                  label: "Speichern",
                  onClick: async () => {
                    await onÄndernPerson({
                      ...(draft as PersonDetail),
                      geburtsdatum: normalizedDate ?? undefined, // 🔑 FIX
                    });
                    setEditMode(false);
                  },
                },
                {
                  label: "Abbrechen",
                  variant: "outlined",
                  onClick: () => {
                    setDraft(selectedPerson);
                    setEditMode(false);
                  },
                },
              ]
            : [
                { label: "Neue Person", onClick: onNeuePerson },
                {
                  label: "Bearbeiten",
                  variant: "outlined",
                  disabled: btnÄndernPerson,
                  onClick: () => setEditMode(true),
                },
                {
                  label: "Löschen",
                  variant: "outlined",
                  color: "error",
                  disabled: btnLöschenPerson,
                  onClick: () => setConfirmOpen(true),
                },
                { label: "Zurück", onClick: onStartMenue },
              ]
        }
      />

      {/* ================= DELETE ================= */}

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Löschen bestätigen</DialogTitle>
        <DialogContent>
          Soll die Person „{selectedPerson.name}“ wirklich gelöscht werden?
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Abbrechen</Button>
          <Button color="error" onClick={onDeletePerson}>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      {/* ================= ADD VEREIN ================= */}

      <Dialog open={addVereinOpen} onClose={() => setAddVereinOpen(false)}>
        <DialogTitle>Verein zuordnen</DialogTitle>
        <DialogContent>
          <TextField
            select
            fullWidth
            label="Verein"
            value={selectedVereinId}
            onChange={(e) => setSelectedVereinId(Number(e.target.value))}
            sx={{ mt: 1 }}
          >
            {verfügbareVereine.map((v) => (
              <MenuItem key={v.id} value={v.id}>
                {v.name}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setAddVereinOpen(false)}>Abbrechen</Button>
          <Button
            disabled={!selectedVereinId}
            onClick={async () => {
              await createMitglied(selectedPerson!.id!, selectedVereinId as number);

              setAddVereinOpen(false);
              setSelectedVereinId("");

              await reloadSelectedPerson(); // 🔑 DAS ist der Re-Render
            }}
          >
            Zuordnen
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
