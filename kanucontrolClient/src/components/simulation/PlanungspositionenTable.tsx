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

import {
    fontSize,
    padding,
} from "@/theme/ui";

import { FinanzKategorie, kategorieZuTyp } from "@/api/types/finanz";

interface PositionTableRow {
    kategorie: FinanzKategorie;
    betrag: number;
    automatisch?: boolean;
}

interface Props {
    title?: string;
    positionen: PositionTableRow[];
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

export default function PlanungspositionenTable({
    title,
    positionen,
}: Props) {

    const showAutomatisch = positionen.some(
        p => p.automatisch !== undefined
    );

    const sortiertePositionen = [...positionen].sort((a, b) => {

        const typA = kategorieZuTyp[a.kategorie];
        const typB = kategorieZuTyp[b.kategorie];

        // Kosten vor Einnahmen
        if (typA !== typB) {
            return typA === "KOSTEN" ? -1 : 1;
        }

        // Innerhalb der Gruppe alphabetisch
        return a.kategorie.localeCompare(
            b.kategorie,
            "de"
        );
    });

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
                    fontSize: fontSize.table,
                }}
            >
                {title}
            </Typography>

            <Table>

                <TableHead>
                    <TableRow>
                        <TableCell
                            sx={{
                                fontWeight: "bold",
                                fontSize: fontSize.table,
                            }}
                        >
                            Kategorie
                        </TableCell>

                        <TableCell
                            align="right"
                            sx={{
                                fontWeight: "bold",
                                fontSize: fontSize.table,
                            }}
                        >
                            Betrag
                        </TableCell>

                        {showAutomatisch && (
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: "bold",
                                    fontSize: fontSize.table,
                                }}
                            >
                                Automatisch
                            </TableCell>
                        )}
                    </TableRow>
                </TableHead>

                <TableBody>
                    {sortiertePositionen.map((position, index) => {

                        const typ = kategorieZuTyp[position.kategorie];

                        const vorherigerTyp =
                            index === 0
                                ? undefined
                                : kategorieZuTyp[
                                sortiertePositionen[index - 1].kategorie
                                ];

                        return (
                            <>
                                {typ !== vorherigerTyp && (
                                    <TableRow>
                                        <TableCell
                                            colSpan={showAutomatisch ? 3 : 2}
                                            sx={{
                                                bgcolor: "grey.100",
                                                fontWeight: "bold",
                                                fontSize: fontSize.table,
                                            }}
                                        >
                                            {typ === "KOSTEN"
                                                ? "Kosten"
                                                : "Einnahmen"}
                                        </TableCell>
                                    </TableRow>
                                )}

                                <TableRow
                                    key={position.kategorie}
                                    sx={{
                                        "& td": {
                                            fontSize: fontSize.table,
                                            py: padding.table,
                                        },
                                    }}
                                >
                                    <TableCell>
                                        {position.kategorie}
                                    </TableCell>

                                    <TableCell align="right">
                                        {formatEuro(position.betrag)}
                                    </TableCell>

                                    {showAutomatisch && (
                                        <TableCell align="center">
                                            {position.automatisch ? "Ja" : "Nein"}
                                        </TableCell>
                                    )}
                                </TableRow>
                            </>
                        );
                    })}
                </TableBody>

            </Table>
        </TableContainer>

    );

}