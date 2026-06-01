import { Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import Money from "@/components/common/Money";

interface Props {
  type: "EINNAHMEN" | "AUSGABEN";
}

const PlanungTable = ({ type }: Props) => {
  const rows =
    type === "EINNAHMEN"
      ? [
          { kategorie: "Teilnehmerbeitrag", betrag: 1200 },
          { kategorie: "KJFP Zuschuss", betrag: 600 },
        ]
      : [
          { kategorie: "Unterkunft", betrag: 800 },
          { kategorie: "Verpflegung", betrag: 600 },
        ];

  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Kategorie</TableCell>
          <TableCell align="right">Betrag</TableCell>
          <TableCell></TableCell>
        </TableRow>
      </TableHead>

      <TableBody>
        {rows.map((r, i) => (
          <TableRow key={i}>
            <TableCell>{r.kategorie}</TableCell>

            <TableCell align="right">
              <Money value={type === "AUSGABEN" ? -r.betrag : r.betrag} colorize />
            </TableCell>

            <TableCell>bearbeiten</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

export default PlanungTable;
