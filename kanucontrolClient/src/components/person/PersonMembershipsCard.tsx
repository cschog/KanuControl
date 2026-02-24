import React from "react";
import {
  Card,
  CardHeader,
  CardContent,
  Stack,
  Box,
  Typography,
  Button,
  MenuItem,
  Select,
} from "@mui/material";
import StarIcon from "@mui/icons-material/Star";

import { PersonDetail } from "@/api/types/Person";
import { MitgliedFunktion, MitgliedFunktionLabel } from "@/api/types/MitgliedFunktion";

interface PersonMembershipsCardProps {
  person: PersonDetail;
  editMode: boolean;
  onSetHauptverein: (mitgliedId: number) => void;
  onDeleteMitglied: (mitgliedId: number) => void;
  onChangeFunktion: (mitgliedId: number, funktion: MitgliedFunktion | null) => void;
}

export const PersonMembershipsCard: React.FC<PersonMembershipsCardProps> = ({
  person,
  editMode,
  onSetHauptverein,
  onDeleteMitglied,
  onChangeFunktion,
}) => {
  if (!person.mitgliedschaften.length) {
    return null;
  }

  return (
    <Card sx={{ mt: 3 }}>
      <CardHeader title="Vereine" />
      <CardContent>
        <Stack spacing={1}>
          {person.mitgliedschaften.map((m) => (
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
              {/* LEFT */}
              <Stack direction="row" spacing={1} alignItems="center">
                {m.hauptVerein && <StarIcon fontSize="small" color="warning" />}

                <Typography>
                  {m.verein.name}
                  {m.verein.abk && ` (${m.verein.abk})`}

                  {/* ⭐ Funktion anzeigen im View-Mode */}
                  {!editMode && m.funktion && (
                    <Typography component="span" color="text.secondary">
                      {" — "}
                      {MitgliedFunktionLabel[m.funktion]}
                    </Typography>
                  )}
                </Typography>
              </Stack>

              {/* RIGHT */}
              {editMode && (
                <Stack direction="row" spacing={1} alignItems="center">
                  {/* FUNKTION */}
                  <Select
                    size="small"
                    value={m.funktion ?? ""}
                    displayEmpty
                    sx={{ minWidth: 180 }}
                    onChange={(e) =>
                      onChangeFunktion(m.id, (e.target.value || null) as MitgliedFunktion | null)
                    }
                  >
                    <MenuItem value="">— keine Funktion —</MenuItem>

                    {Object.entries(MitgliedFunktionLabel).map(([key, label]) => (
                      <MenuItem key={key} value={key}>
                        {label}
                      </MenuItem>
                    ))}
                  </Select>

                  {/* HAUPTVEREIN */}
                  {!m.hauptVerein && (
                    <Button size="small" onClick={() => onSetHauptverein(m.id)}>
                      Hauptverein
                    </Button>
                  )}

                  {/* DELETE */}
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
  );
};
