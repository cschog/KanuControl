import React from "react";
import { Card, CardHeader, CardContent, Stack, Box, Typography, Button } from "@mui/material";
import StarIcon from "@mui/icons-material/Star";

import { PersonDetail } from "@/api/types/Person";

interface PersonMembershipsCardProps {
  person: PersonDetail;
  editMode: boolean;
  onSetHauptverein: (mitgliedId: number) => void;
  onDeleteMitglied: (mitgliedId: number) => void;
}

export const PersonMembershipsCard: React.FC<PersonMembershipsCardProps> = ({
  person,
  editMode,
  onSetHauptverein,
  onDeleteMitglied,
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
  );
};
