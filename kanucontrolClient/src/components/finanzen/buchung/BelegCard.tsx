import { Box, Button, Paper, Stack, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { buchungColumns } from "@/components/finanzen/buchung/buchungColumns";

import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";

import { kategorieZuTyp } from "@/api/types/finanz";

interface Props {
  beleg: AbrechnungBeleg;

  readOnly?: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;

  onAddPosition: (beleg: AbrechnungBeleg) => void;

  onEditPosition: (beleg: AbrechnungBeleg, buchung: Buchung) => void;

  onDeletePosition: (belegId: number, buchungId: number) => void;

  onDeleteBeleg: (belegId: number) => void;
}

export default function BelegCard({
  beleg,
  readOnly = false,
  onEditBeleg,
  onAddPosition,
  onEditPosition,
  onDeletePosition,
  onDeleteBeleg,
}: Props) {
  const columns = buchungColumns({
    onEdit: (buchung) => onEditPosition(beleg, buchung),

    onDelete: (buchungId) => onDeletePosition(beleg.id, buchungId),
  });

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        mb: 3,
      }}
    >
      {/* =====================================================
          HEADER
         ===================================================== */}

      <Stack
        direction={{ xs: "column", md: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", md: "center" }}
        spacing={2}
        mb={2}
      >
        <Box>
          <Typography
            sx={{
              fontSize: {
                xs: "0.9rem",
                sm: "1rem",
                md: "1.3rem",
              },

              fontWeight: 600,

              whiteSpace: "nowrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
            }}
          >
            {beleg.belegnummer}
            {" ("}
            {beleg.kuerzel}
            {") • "}
            {beleg.datum}
            {" • "}
            {beleg.beschreibung}
          </Typography>

          <Button size="small" onClick={() => onEditBeleg(beleg)}>
            Bearbeiten
          </Button>
        </Box>

        {!readOnly && (
          <Stack direction="row" spacing={1}>
            <Button size="small" variant="contained" onClick={() => onAddPosition(beleg)}>
              + Position
            </Button>

            <Button size="small" color="error" onClick={() => onDeleteBeleg(beleg.id)}>
              Löschen
            </Button>
          </Stack>
        )}
      </Stack>

      {/* =====================================================
          TABLE
         ===================================================== */}

      <GenericTableTanstack<Buchung>
        data={beleg.positionen}
        columns={columns}
        loading={false}
        height={350}
        mobileRenderRow={(row) => (
          <Box>
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 2,
              }}
            >
              <Typography
                sx={{
                  fontSize: "0.7rem",
                  fontWeight: 600,
                }}
              >
                {row.kategorie.replaceAll("_", " ")}
              </Typography>

              <Typography
                sx={{
                  fontSize: "0.9rem",
                  fontWeight: 700,
                  whiteSpace: "nowrap",

                  color: kategorieZuTyp[row.kategorie] === "KOSTEN" ? "error.main" : "success.main",
                }}
              >
                {kategorieZuTyp[row.kategorie] === "KOSTEN" ? "-" : "+"}
                {row.betrag.toFixed(2)} €
              </Typography>
            </Box>

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                fontSize: "0.8rem",
              }}
            >
              {row.beschreibung}
            </Typography>

            {!readOnly && (
              <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={(e) => {
                    e.stopPropagation();

                    onEditPosition(beleg, row);
                  }}
                >
                  Bearbeiten
                </Button>

                <Button
                  size="small"
                  color="error"
                  variant="outlined"
                  onClick={(e) => {
                    e.stopPropagation();

                    onDeletePosition(beleg.id, row.id);
                  }}
                >
                  Löschen
                </Button>
              </Stack>
            )}
          </Box>
        )}
      />
    </Paper>
  );
}
