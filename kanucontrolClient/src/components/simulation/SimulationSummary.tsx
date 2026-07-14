import {
    Box,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@mui/material";

import Money from "@/components/common/Money";

import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import PaymentsIcon from "@mui/icons-material/Payments";
import BalanceIcon from "@mui/icons-material/Balance";
import VolunteerActivismIcon from "@mui/icons-material/VolunteerActivism";

import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";

interface SimulationSummaryProps {
    ergebnis: SimulationErgebnis;
}

function formatEuro(value: number): string {
    return new Intl.NumberFormat("de-DE", {
        style: "currency",
        currency: "EUR",
    }).format(value);
}

interface KpiCardProps {
    title: string;
    value: React.ReactNode;
    icon: React.ReactNode;
}

function KpiCard({
    title,
    value,
    icon,
}: KpiCardProps) {
    return (
        <Card sx={{ height: "100%" }}>
            <CardContent

                sx={{
                    py: { xs: 1, sm: 2 },
                    px: { xs: 1.5, sm: 2 },
                    "&:last-child": {
                        pb: { xs: 1, sm: 2 },
                    },
                }}
            >
                <Box
                    display="flex"
                    alignItems="center"
                    justifyContent="center"
                    gap={1}
                >
                    {icon}
                    <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{
                            fontSize: {
                                xs: "0.70rem",
                                sm: "0.85rem",
                            },
                        }}
                    >
                        {title}
                    </Typography>
                </Box>
                <Box
                    display="flex"
                    justifyContent="center"
                    mt={1}
                >
                    {value}
                </Box>
            </CardContent>
        </Card>
    );
}

export default function SimulationSummary({
    ergebnis,
}: SimulationSummaryProps) {

    const zuschuss = ergebnis.positionen
        .filter(position => position.kategorie === "KJFP_ZUSCHUSS")
        .reduce((summe, position) => summe + position.betrag, 0);

    return (
        <Grid container spacing={2} sx={{ mb: 2 }}>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Gesamtkosten"
                    value={
                        <Money
                            value={ergebnis.kosten}
                            variant="h5"
                            align="center"
                        />
                    }
                    icon={(
                        <AccountBalanceWalletIcon
                            color="primary"
                            fontSize="small"
                        />
                    )}
                />
            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Gesamteinnahmen"
                    value={
                        <Money
                            value={ergebnis.einnahmen}
                            variant="h5"
                            align="center"
                        />
                    }
                    icon={(
                        <PaymentsIcon color="success" fontSize="small" />
                    )}
                />

            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Eigenanteil"
                    value={
                        <Money
                            value={ergebnis.saldo}
                            variant="h5"
                            align="center"
                            colorize
                        />
                    }
                    icon={(
                        <BalanceIcon fontSize="small"/>
                    )}
                />
            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="KJFP-Zuschuss"
                    value={
                        <Money
                            value={zuschuss}
                            variant="h5"
                            align="center"
                        />
                    }
                    icon={<VolunteerActivismIcon color="primary" fontSize="small" />}
                />
            </Grid>

        </Grid>
    );
}