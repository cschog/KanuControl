import {
    Box,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@mui/material";

import Money from "@/components/common/Money";

import {
    fontSize,
    padding,
    radius,
} from "@/theme/ui";

import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import PaymentsIcon from "@mui/icons-material/Payments";
import BalanceIcon from "@mui/icons-material/Balance";
import VolunteerActivismIcon from "@mui/icons-material/VolunteerActivism";

interface Props {
    kosten: number;
    einnahmen: number;
    eigenanteil: number;
    kjfpZuschuss: number;
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
        <Card
            sx={{
                height: "100%",
                borderRadius: radius.card,
            }}
        >
            <CardContent
                sx={{
                    py: padding.card,
                    px: padding.card,
                    "&:last-child": {
                        pb: padding.card,
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
                            fontSize: fontSize.cardTitle,
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

export default function FinanzSummary({
    kosten,
    einnahmen,
    eigenanteil,
    kjfpZuschuss,
}: Props) {

    const eigenanteilColor =
        eigenanteil <= -500
            ? "success.main"
            : eigenanteil <= -250
                ? "warning.main"
                : "error.main";

    return (
        <Grid
            container
            spacing={2}
            sx={{ mb: 2 }}
        >
            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Gesamtkosten"
                    value={
                        <Money
                            value={kosten}
                            variant="h5"
                            align="center"
                            sx={{
                                fontSize: fontSize.kpi,
                            }}
                        />
                    }
                    icon={
                        <AccountBalanceWalletIcon
                            color="primary"
                            fontSize="small"
                        />
                    }
                />
            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Gesamteinnahmen"
                    value={
                        <Money
                            value={einnahmen}
                            variant="h5"
                            align="center"
                            sx={{
                                fontSize: fontSize.kpi,
                            }}
                        />
                    }
                    icon={
                        <PaymentsIcon
                            color="success"
                            fontSize="small"
                        />
                    }
                />
            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="Eigenanteil"
                    value={
                        <Money
                            value={eigenanteil}
                            variant="h5"
                            align="center"
                            sx={{
                                color: eigenanteilColor,
                                fontSize: fontSize.kpi,
                            }}
                        />
                    }
                    icon={
                        <BalanceIcon fontSize="small" />
                    }
                />
            </Grid>

            <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                <KpiCard
                    title="KJFP-Zuschuss"
                    value={
                        <Money
                            value={kjfpZuschuss}
                            variant="h5"
                            align="center"
                            sx={{
                                fontSize: fontSize.kpi,
                            }}
                        />
                    }
                    icon={
                        <VolunteerActivismIcon
                            color="primary"
                            fontSize="small"
                        />
                    }
                />
            </Grid>
        </Grid>
    );
}