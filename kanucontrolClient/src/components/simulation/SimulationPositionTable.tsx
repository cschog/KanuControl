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

    const tableFontSize = {
        xs: "1rem",
        lg: "1.15rem",
        xl: "1.25rem",
    };

    const tablePadding = {
        xs: 1.5,
        lg: 2,
    };

    return (

        <TableContainer
            component={Paper}
            sx={{ mt: 2 }}
        >
            <Typography
                variant="h5"
                sx={{
                    p: 2,
                    fontWeight: "bold",
                    fontSize: tableFontSize,
                }}
            >
                Berechnungspositionen
            </Typography>

            <Table>

                <TableHead>
                    <TableRow>
                        <TableCell
                            sx={{
                                fontWeight: "bold",
                                fontSize: tableFontSize,
                            }}
                        >
                            Kategorie
                        </TableCell>

                        <TableCell
                            align="right"
                            sx={{
                                fontWeight: "bold",
                                fontSize: tableFontSize,
                            }}
                        >
                            Betrag
                        </TableCell>

                        <TableCell
                            align="center"
                            sx={{
                                fontWeight: "bold",
                                fontSize: tableFontSize,
                            }}
                        >
                            Automatisch
                        </TableCell>
                    </TableRow>
                </TableHead>

                <TableBody>
                    {positionen.map((position) => (
                        <TableRow
                            key={position.kategorie}
                            sx={{
                                "& td": {
                                    fontSize: tableFontSize,
                                    py: tablePadding,
                                },
                            }}
                        >
                            <TableCell>
                                {position.kategorie}
                            </TableCell>

                            <TableCell align="right">
                                {formatEuro(position.betrag)}
                            </TableCell>

                            <TableCell align="center">
                                {position.automatisch ? "Ja" : "Nein"}
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>

            </Table>
        </TableContainer>

    );

}