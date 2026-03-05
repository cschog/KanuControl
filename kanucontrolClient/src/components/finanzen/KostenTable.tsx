import { Table, TableHead, TableRow, TableCell, TableBody, IconButton } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import TableContainer from "@mui/material/TableContainer";

interface Props {
  onDelete: (id: number) => void;
}

const rows = [
  {
    id: 1,
    datum: "01.05.2025",
    person: "MS",
    kategorie: "Unterkunft",
    kommentar: "Jugendherberge",
    einnahme: "",
    ausgabe: "800 €",
  },
  {
    id: 2,
    datum: "02.05.2025",
    person: "AB",
    kategorie: "Verpflegung",
    kommentar: "Essen",
    einnahme: "",
    ausgabe: "600 €",
  },
];

const KostenTable = ({ onDelete }: Props) => {
  const handleEdit = (id: number) => {
    console.log("Edit Buchung", id);
  };

  return (
    <TableContainer
      sx={{
        maxHeight: 400,
      }}
    >
      <Table size="small" stickyHeader>
        <TableHead>
          <TableRow>
            <TableCell>Datum</TableCell>
            <TableCell>Person</TableCell>
            <TableCell>Kategorie</TableCell>
            <TableCell>Kommentar</TableCell>
            <TableCell align="right">Einnahme</TableCell>
            <TableCell align="right">Ausgabe</TableCell>
            <TableCell align="center">Aktionen</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {rows.map((r) => (
            <TableRow key={r.id}>
              <TableCell>{r.datum}</TableCell>
              <TableCell>{r.person}</TableCell>
              <TableCell>{r.kategorie}</TableCell>
              <TableCell>{r.kommentar}</TableCell>
              <TableCell align="right">{r.einnahme}</TableCell>
              <TableCell align="right">{r.ausgabe}</TableCell>

              <TableCell align="center">
                <IconButton size="small" onClick={() => handleEdit(r.id)}>
                  <EditIcon fontSize="small" />
                </IconButton>

                <IconButton size="small" onClick={() => onDelete(r.id)}>
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default KostenTable;
