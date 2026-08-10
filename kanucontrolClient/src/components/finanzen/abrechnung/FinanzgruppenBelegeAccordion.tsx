import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { Accordion, AccordionDetails, AccordionSummary, Stack, Typography } from "@mui/material";

import Money from "@/components/common/Money";
import MultiBelegAccordion from "@/components/finanzen/abrechnung/MultiBelegAccordion";
import SingleBelegRow from "@/components/finanzen/abrechnung/SingleBelegRow";

import { berechneBelegsumme, istInBeleglisteSichtbar } from "@/api/utils/belegUtils";
import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";

interface Props {
  belege: AbrechnungBeleg[];
  readOnly: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;
  onShowDokumente: (beleg: AbrechnungBeleg) => void;
  onAddPosition: (beleg: AbrechnungBeleg) => void;
  onEditPosition: (beleg: AbrechnungBeleg, buchung: Buchung) => void;
  onDeletePosition: (belegId: number, buchung: Buchung) => void;
  onDeleteBeleg: (beleg: AbrechnungBeleg) => void;
}

export default function FinanzgruppenBelegeAccordion({
  belege,
  readOnly,
  onEditBeleg,
  onShowDokumente,
  onAddPosition,
  onEditPosition,
  onDeletePosition,
  onDeleteBeleg,
}: Props) {
  const gruppen = new Map<string, AbrechnungBeleg[]>();

  [...belege]
    .sort((a, b) => {
      const cmp = a.kuerzel.localeCompare(b.kuerzel);
      if (cmp !== 0) {
        return cmp;
      }

      return b.id - a.id;
    })
    .forEach((beleg) => {
      const sichtbarePositionen = beleg.positionen.filter(istInBeleglisteSichtbar);

      if (sichtbarePositionen.length === 0) {
        return;
      }

      const sichtbarerBeleg = {
        ...beleg,
        positionen: sichtbarePositionen,
      };

      if (!gruppen.has(beleg.kuerzel)) {
        gruppen.set(beleg.kuerzel, []);
      }

      gruppen.get(beleg.kuerzel)!.push(sichtbarerBeleg);
    });

  return (
    <>
      {[...gruppen.entries()].map(([kuerzel, gruppe]) => {
        const summe = gruppe.reduce((sum, beleg) => sum + berechneBelegsumme(beleg), 0);

        return (
          <Accordion key={kuerzel} >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                width="100%"
                pr={2}
              >
                <Typography fontWeight={700}>
                  {kuerzel} ({gruppe.length} {gruppe.length === 1 ? "Beleg" : "Belege"})
                </Typography>

                <Money value={summe} />
              </Stack>
            </AccordionSummary>

            <AccordionDetails>
              {gruppe.map((beleg) => {
                const Component =
                  beleg.positionen.length > 1 ? MultiBelegAccordion : SingleBelegRow;

                return (
                  <Component
                    key={beleg.id}
                    beleg={beleg}
                    readOnly={readOnly}
                    onEditBeleg={onEditBeleg}
                    onShowDokumente={onShowDokumente}
                    onAddPosition={onAddPosition}
                    onEditPosition={onEditPosition}
                    onDeletePosition={onDeletePosition}
                    onDeleteBeleg={onDeleteBeleg}
                  />
                );
              })}
            </AccordionDetails>
          </Accordion>
        );
      })}
    </>
  );
}
