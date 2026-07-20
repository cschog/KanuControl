import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Box,
    Grid,
    Paper,
    Typography,
    useTheme,
    useMediaQuery,
} from "@mui/material";

import { VeranstaltungsInfo } from "@/api/types/simulation/VeranstaltungsInfo";
import { radius } from "@/theme/ui";

interface RahmendatenAccordionProps {
    veranstaltung: VeranstaltungsInfo;
}

function MobileInfoRow({
    label,
    value,
}: {
    label: string;
    value?: string | number | null;
}) {
    return (
        <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            py={0.75}
            sx={{
                borderBottom: "1px solid",
                borderColor: "divider",
                "&:last-child": {
                    borderBottom: "none",
                },
            }}
        >
            <Typography
                variant="body2"
                color="text.secondary"
            >
                {label}
            </Typography>

            <Typography
                variant="body2"
                fontWeight={600}
            >
                {value || "—"}
            </Typography>
        </Box>
    );
}

export default function RahmendatenAccordion({
    veranstaltung,
}: RahmendatenAccordionProps) {

    const infoSize = {
        xs: 12,
        sm: 6,
        md: 4,
        lg: 3,
    };

    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

    const InfoCard = ({
        titel,
        wert,
    }: {
        titel: string;
        wert?: string | number | null;
    }) => (
        <Grid size={infoSize}>
            <Paper
                variant="outlined"
                sx={{
                    p: 2,
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    borderRadius: radius.dialog,
                }}
            >
                <Typography
                    variant="caption"
                    color="text.secondary"
                >
                    {titel}
                </Typography>

                <Typography
                    variant="subtitle1"
                    fontWeight={600}
                >
                    {wert || "—"}
                </Typography>
            </Paper>
        </Grid>
    );

    return (
        <Accordion
            defaultExpanded={false}
            sx={{
                mb: 2,
                bgcolor: "grey.100",
                borderRadius: radius.dialog,
            }}
        >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant={isMobile ? "h6" : "h5"}>
                    Rahmendaten
                </Typography>
            </AccordionSummary>

           <AccordionDetails
    sx={{
        bgcolor: "grey.300",
        borderTop: 1,
        borderColor: "divider",
    }}
>
    {isMobile ? (
        <>
            <MobileInfoRow
                label="Veranstaltung"
                value={veranstaltung.name}
            />

            <MobileInfoRow
                label="Zeitraum"
                value={`${veranstaltung.beginnDatum} – ${veranstaltung.endeDatum}`}
            />

            <MobileInfoRow
                label="Typ"
                value={veranstaltung.typ}
            />

            <MobileInfoRow
                label="Tage / Nächte"
                value={`${veranstaltung.tage} / ${veranstaltung.naechte}`}
            />

            <MobileInfoRow
                label="KiK"
                value={
                    veranstaltung.vereinKikZertifiziert
                        ? "Ja"
                        : "Nein"
                }
            />

            <MobileInfoRow
                label="Unterkunft"
                value={veranstaltung.unterkunftsartName}
            />

            <MobileInfoRow
                label="Verpflegung"
                value={veranstaltung.verpflegungsmodellName}
            />

            <MobileInfoRow
                label="Beiträge"
                value={veranstaltung.beitragsstrukturName}
            />
        </>
    ) : (
        <Grid container spacing={2}>
            <InfoCard
                titel="Veranstaltung"
                wert={veranstaltung.name}
            />

            <InfoCard
                titel="Zeitraum"
                wert={`${veranstaltung.beginnDatum} – ${veranstaltung.endeDatum}`}
            />

            <InfoCard
                titel="Veranstaltungstyp"
                wert={veranstaltung.typ}
            />

            <InfoCard
                titel="Tage / Nächte"
                wert={`${veranstaltung.tage} / ${veranstaltung.naechte}`}
            />

            <InfoCard
                titel="KiK-Zertifiziert"
                wert={
                    veranstaltung.vereinKikZertifiziert
                        ? "Ja"
                        : "Nein"
                }
            />

            <InfoCard
                titel="Unterkunft"
                wert={veranstaltung.unterkunftsartName}
            />

            <InfoCard
                titel="Verpflegung"
                wert={veranstaltung.verpflegungsmodellName}
            />

            <InfoCard
                titel="Beitragsstruktur"
                wert={veranstaltung.beitragsstrukturName}
            />
        </Grid>
    )}
</AccordionDetails>
        </Accordion>
    );
}