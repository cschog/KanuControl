import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { Accordion, AccordionDetails, AccordionSummary, Box } from "@mui/material";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { buchungColumns } from "@/components/finanzen/abrechnung/buchungColumns";
import BuchungRow from "@/components/finanzen/abrechnung/BuchungRow";

import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";

import { radius, spacing } from "@/theme/ui";
import { berechneBelegsumme } from "@/api/utils/belegUtils";
import BelegHeader from "@/components/finanzen/abrechnung/BelegHeader";

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

export default function MultiBelegAccordion({
  beleg,
  readOnly = false,
  onEditBeleg,
  onAddPosition,
  onEditPosition,
  onDeletePosition,
  onDeleteBeleg,
  onShowDokumente,
}: Props) {
  if (beleg.positionen.length === 0) {
    return null;
  }

  const columns = buchungColumns({
    onEdit: (buchung) => onEditPosition(beleg, buchung),
    onDelete: (buchung) => onDeletePosition(beleg.id, buchung),
  });

  const summe = berechneBelegsumme(beleg);

  return (
    <Accordion
      disableGutters
      sx={{
        mb: spacing.card,
        borderRadius: radius.card,
      }}
    >
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <BelegHeader
          beleg={beleg}
          summe={summe}
          readOnly={readOnly}
          showBuchungsChip
          onEditBeleg={onEditBeleg}
          onAddPosition={onAddPosition}
          onDeleteBeleg={onDeleteBeleg}
          onShowDokumente={onShowDokumente}
        />
      </AccordionSummary>

      <AccordionDetails>
        <Box sx={{ mt: spacing.card }}>
          <GenericTableTanstack<Buchung>
            data={beleg.positionen}
            columns={columns}
            loading={false}
            height={350}
            mobileRenderRow={(row) => (
              <BuchungRow
                beleg={beleg}
                buchung={row}
                readOnly={readOnly}
                stopPropagation
                onEdit={onEditPosition}
                onDelete={onDeletePosition}
              />
            )}
          />
        </Box>
      </AccordionDetails>
    </Accordion>
  );
}
