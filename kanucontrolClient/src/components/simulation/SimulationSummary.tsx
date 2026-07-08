import {
    Card,
    CardContent,
    Grid,
    Typography,
} from "@mui/material";

import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";

interface SimulationSummaryProps {

    ergebnis: SimulationErgebnis;

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

export default function SimulationSummary({

    ergebnis,

}: SimulationSummaryProps) {

    return (

        <Grid
            container
            spacing={2}
        >

            <Grid size={{ xs: 12, md: 4 }}>

                <Card>

                    <CardContent>

                        <Typography
                            variant="subtitle2"
                            color="text.secondary"
                        >
                            Gesamtkosten
                        </Typography>

                        <Typography
                            variant="h4"
                        >
                            {formatEuro(ergebnis.kosten)}
                        </Typography>

                    </CardContent>

                </Card>

            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>

                <Card>

                    <CardContent>

                        <Typography
                            variant="subtitle2"
                            color="text.secondary"
                        >
                            Gesamteinnahmen
                        </Typography>

                        <Typography
                            variant="h4"
                        >
                            {formatEuro(ergebnis.einnahmen)}
                        </Typography>

                    </CardContent>

                </Card>

            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>

                <Card>

                    <CardContent>

                        <Typography
                            variant="subtitle2"
                            color="text.secondary"
                        >
                            Eigenleistung
                        </Typography>

                        <Typography
                            variant="h4"
                            color={
                                ergebnis.saldo >= 0
                                    ? "success.main"
                                    : "error.main"
                            }
                        >
                            {formatEuro(ergebnis.saldo)}
                        </Typography>

                    </CardContent>
                </Card>
            </Grid>
        </Grid>

    );

}