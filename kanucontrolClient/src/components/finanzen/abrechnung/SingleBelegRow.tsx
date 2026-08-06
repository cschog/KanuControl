import { Paper, Stack } from "@mui/material";

import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";
import BuchungRow from "@/components/finanzen/abrechnung/BuchungRow";
import BelegHeader from "@/components/finanzen/abrechnung/BelegHeader";
import { berechneBelegsumme } from "@/api/utils/belegUtils";

interface Props {
  beleg: AbrechnungBeleg;
  readOnly?: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;
  onAddPosition: (beleg: AbrechnungBeleg) => void;
  onEditPosition: (beleg: AbrechnungBeleg, buchung: Buchung) => void;
  onDeletePosition: (belegId: number, buchung: Buchung) => void;
  onDeleteBeleg: (beleg: AbrechnungBeleg) => void;
  onShowDokumente: (beleg: AbrechnungBeleg) => void;
}

export default function SingleBelegRow({
  beleg,
  readOnly = false,
  onEditBeleg,
  onAddPosition,
  onEditPosition,
  onDeletePosition,
  onDeleteBeleg,
  onShowDokumente,
}: Props) {
  const buchung = beleg.positionen[0];
  const summe = berechneBelegsumme(beleg);

  if (!buchung) return null;

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.5,
        mb: 1.5,
      }}
    >
      <Stack spacing={2}>
        <Stack direction={{ xs: "column", md: "row" }}>
          <BelegHeader
            beleg={beleg}
            summe={summe}
            readOnly={readOnly}
            onEditBeleg={onEditBeleg}
            onAddPosition={onAddPosition}
            onDeleteBeleg={onDeleteBeleg}
            onShowDokumente={onShowDokumente}
          />
        </Stack>

        <BuchungRow
          beleg={beleg}
          buchung={buchung}
          readOnly={readOnly}
          onEdit={onEditPosition}
          onDelete={onDeletePosition}
        />
      </Stack>
    </Paper>
  );
}
