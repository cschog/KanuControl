import { Button, Stack, Typography } from "@mui/material";

import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";
import { kategorieZuTyp } from "@/api/types/finanz";
import { istEditierbar } from "@/api/utils/buchungUtils";
import Money from "@/components/common/Money";
import { fontSize } from "@/theme/ui";

interface Props {
  beleg: AbrechnungBeleg;
  buchung: Buchung;
  readOnly?: boolean;
  stopPropagation?: boolean;

  onEdit: (beleg: AbrechnungBeleg, buchung: Buchung) => void;
  onDelete: (belegId: number, buchungId: number) => void;
}

export default function BuchungRow({
  beleg,
  buchung,
  readOnly = false,
  onEdit,
  onDelete,
  stopPropagation = false,
}: Props) {
  const typ = kategorieZuTyp[buchung.kategorie];

  const showBetrag = beleg.positionen.length > 1;
  const editierbar = !readOnly && istEditierbar(buchung);


  return (
    <Stack spacing={0.75}>
      {/* Name + Betrag */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" gap={1}>
        <Typography
          sx={{
            flex: 1,
            minWidth: 0,
            fontWeight: 500,
          }}
          noWrap
        >
          {buchung.beschreibung}
        </Typography>

        {showBetrag && (
          <Money
            value={typ === "KOSTEN" ? -buchung.betrag : buchung.betrag}
            colorize
            align="right"
            sx={{
              fontSize: fontSize.money,
              fontWeight: 700,
              flexShrink: 0,
            }}
          />
        )}
      </Stack>

      {/* Nur manuelle Buchungen */}
      {editierbar && (
        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">

          <Button
            size="small"
            variant="outlined"
            onClick={(e) => {
              if (stopPropagation) e.stopPropagation();
              onEdit(beleg, buchung);
            }}
          >
            Bearbeiten
          </Button>

          <Button
            size="small"
            color="error"
            variant="outlined"
            onClick={(e) => {
              if (stopPropagation) e.stopPropagation();
              onDelete(beleg.id, buchung.id);
            }}
          >
            Löschen
          </Button>
        </Stack>
      )}
    </Stack>
  );
}
