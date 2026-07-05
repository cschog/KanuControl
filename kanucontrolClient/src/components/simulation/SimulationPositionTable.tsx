import {
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from "@mui/material";

import { SimulationPosition } from "@/api/types/simulation/SimulationPosition";

interface SimulationPositionTableProps {

    positionen: SimulationPosition[];

}

function formatEuro(value: number): string {

    return new Intl.NumberFormat(
        "de-DE",
        {
            style: "currency",
            currency: "EUR",
        }
    ).format(value);

}

export default function SimulationPositionTable({

    positionen,

}: SimulationPositionTableProps) {

    return (

        <TableContainer
            component={Paper}
            sx={{ mt: 2 }}
        >

            <Typography
                variant="h6"
                sx={{ p: 2 }}
            >
                Positionen
            </Typography>

            <Table>

                <TableHead>

                    <TableRow>

                        <TableCell>
                            Kategorie
                        </TableCell>

                        <TableCell align="right">
                            Betrag
                        </TableCell>

                        <TableCell align="center">
                            Automatisch
                        </TableCell>

                    </TableRow>

                </TableHead>

                <TableBody>

                    {positionen.map((position) => (

                        <TableRow
                            key={position.kategorie}
                        >

                            <TableCell>
                                {position.kategorie}
                            </TableCell>

                            <TableCell align="right">
                                {formatEuro(position.betrag)}
                            </TableCell>

                            <TableCell align="center">
                                {position.automatisch
                                    ? "Ja"
                                    : "Nein"}
                            </TableCell>

                        </TableRow>

                    ))}

                </TableBody>

            </Table>

        </TableContainer>

    );

}