import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Button,
  TextField,
  Stack,
  Card,
  CardHeader,
  MenuItem,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";
import StarIcon from "@mui/icons-material/Star";

import { BottomActionBar } from "@/components/common/BottomActionBar";
import { PersonDetail, PersonSave } from "@/api/types/Person";
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { normalizeGermanDate } from "@/utils/dateUtils";

/* =========================================================
   Props
   ========================================================= */

interface PersonFormViewProps {
  personDetail: PersonDetail | null;
  draftPerson: PersonSave | null;
  editMode: boolean;

  onNeuePerson: () => void;
  onEdit: () => void;
  onCancelEdit: () => void;
  onSpeichern: (person: PersonSave) => Promise<void>;

  onDeletePerson: () => void;
  onDeleteMitglied: (mitgliedId: number) => Promise<void>;
  onSetHauptverein: (mitgliedId: number) => Promise<void>;

  onStartMenue: () => void;

  btnÄndernPerson: boolean;
  btnLöschenPerson: boolean;
}

/* =========================================================
   Helper
   ========================================================= */

function mapDetailToSave(detail: PersonDetail): PersonSave {
  return {
    vorname: detail.vorname,
    name: detail.name,
    sex: detail.sex,
    geburtsdatum: detail.geburtsdatum,
    telefon: detail.telefon,
    telefonFestnetz: detail.telefonFestnetz,
    strasse: detail.strasse,
    plz: detail.plz,
    ort: detail.ort,
    countryCode: detail.countryCode,
    bankName: detail.bankName,
    iban: detail.iban,
    aktiv: detail.aktiv,
    mitgliedschaften: detail.mitgliedschaften.map((m) => ({
      vereinId: m.verein.id,
      hauptVerein: m.hauptVerein,
      funktion: m.funktion,
    })),
  };
}

/* =========================================================
   Component
   ========================================================= */

export const PersonFormView: React.FC<PersonFormViewProps> = ({
  personDetail,
  draftPerson,
  editMode,
  onNeuePerson,
  onEdit,
  onCancelEdit,
  onSpeichern,
  onDeletePerson,
  onDeleteMitglied,
  onSetHauptverein,
  onStartMenue,
  btnÄndernPerson,
  btnLöschenPerson,
}) => {
  const [form, setForm] = useState<PersonSave | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  useEffect(() => {
    if (editMode && draftPerson) {
      setForm(draftPerson);
    } else if (personDetail) {
      setForm(mapDetailToSave(personDetail));
    } else {
      setForm(null);
    }
  }, [editMode, draftPerson, personDetail]);

  if (!form) {
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

  const dateValue =
    form.geburtsdatum && /^\d{4}-\d{2}-\d{2}$/.test(form.geburtsdatum) ? form.geburtsdatum : "";

  const update = <K extends keyof PersonSave>(key: K, value: PersonSave[K]) =>
    setForm((f) => (f ? { ...f, [key]: value } : f));

  return (
    <>
      {/* ================= PERSON ================= */}

      <Box
        display="grid"
        gridTemplateColumns={{ xs: "1fr", sm: "repeat(2,1fr)", md: "repeat(3,1fr)" }}
        gap={1.25}
      >
        <TextField
          label="Vorname"
          value={form.vorname}
          onChange={(e) => update("vorname", e.target.value)}
          disabled={!editMode}
          required
        />
        <TextField
          label="Name"
          value={form.name}
          onChange={(e) => update("name", e.target.value)}
          disabled={!editMode}
          required
        />

        <TextField
          select
          label="Geschlecht"
          value={form.sex}
          onChange={(e) => update("sex", e.target.value as Sex)}
          disabled={!editMode}
          fullWidth
        >
          <MenuItem value="M">Männlich</MenuItem>
          <MenuItem value="W">Weiblich</MenuItem>
          <MenuItem value="D">Divers</MenuItem>
        </TextField>

        <TextField
          type="date"
          label="Geburtsdatum"
          value={dateValue}
          onChange={(e) =>
            update("geburtsdatum", e.target.value === "" ? undefined : e.target.value)
          }
          disabled={!editMode}
          InputLabelProps={{ shrink: true }}
          fullWidth
        />
        <TextField
          label="Ort"
          value={form.ort ?? ""}
          onChange={(e) => update("ort", e.target.value)}
          disabled={!editMode}
        />
        <TextField
          select
          label="Land"
          value={form.countryCode ?? ""}
          onChange={(e) =>
            update(
              "countryCode",
              e.target.value === "" ? undefined : (e.target.value as CountryCode),
            )
          }
          disabled={!editMode}
          fullWidth
        >
          <MenuItem value="">–</MenuItem>
          <MenuItem value="DE">Deutschland</MenuItem>
          <MenuItem value="NL">Niederlande</MenuItem>
          <MenuItem value="BE">Belgien</MenuItem>
        </TextField>
      </Box>

      {/* ================= VEREINE ================= */}

      {personDetail && personDetail.mitgliedschaften.length > 0 && (
        <Card sx={{ mt: 3 }}>
          <CardHeader title="Vereine" />
          <CardContent>
            <Stack spacing={1}>
              {personDetail.mitgliedschaften.map((m) => (
                <Box
                  key={m.id}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    px: 1,
                    py: 0.5,
                    borderRadius: 1,
                    bgcolor: m.hauptVerein ? "action.selected" : "transparent",
                  }}
                >
                  <Stack direction="row" spacing={1} alignItems="center">
                    {m.hauptVerein && <StarIcon fontSize="small" color="warning" />}
                    <Typography>
                      {m.verein.name}
                      {m.verein.abk && ` (${m.verein.abk})`}
                    </Typography>
                  </Stack>

                  {editMode && (
                    <Stack direction="row" spacing={1}>
                      {!m.hauptVerein && (
                        <Button size="small" onClick={() => onSetHauptverein(m.id)}>
                          Hauptverein
                        </Button>
                      )}
                      <Button size="small" color="error" onClick={() => onDeleteMitglied(m.id)}>
                        Entfernen
                      </Button>
                    </Stack>
                  )}
                </Box>
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      {/* ================= ACTION BAR ================= */}

      {editMode ? (
        <BottomActionBar
          left={[
            {
              label: "Speichern",
              onClick: async () => {
                await onSpeichern({
                  ...form,
                  geburtsdatum: normalizeGermanDate(form.geburtsdatum ?? "") ?? undefined,
                });
              },
            },
            {
              label: "Abbrechen",
              variant: "outlined",
              onClick: onCancelEdit,
            },
          ]}
        />
      ) : (
        <BottomActionBar
          left={[
            { label: "Neue Person", onClick: onNeuePerson },
            {
              label: "Bearbeiten",
              variant: "outlined",
              disabled: btnÄndernPerson,
              onClick: onEdit,
            },
            {
              label: "Löschen",
              variant: "outlined",
              color: "error",
              disabled: btnLöschenPerson,
              onClick: () => setConfirmOpen(true),
            },
            { label: "Zurück", onClick: onStartMenue },
          ]}
        />
      )}

      {/* ================= DELETE ================= */}

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Löschen bestätigen</DialogTitle>
        <DialogContent>
          Soll die Person „{personDetail?.name}“ wirklich gelöscht werden?
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Abbrechen</Button>
          <Button color="error" onClick={onDeletePerson}>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
