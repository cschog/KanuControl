import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Grid,
    Paper,
    Typography,
} from "@mui/material";

import { VeranstaltungsInfo } from "@/api/types/simulation/VeranstaltungsInfo";

interface RahmendatenAccordionProps {
    veranstaltung: VeranstaltungsInfo;
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
                    borderRadius: 2,
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
            defaultExpanded
            sx={{
                mb: 2,
                bgcolor: "grey.100",
                borderRadius: 2,
            }}
        >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">
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

                <Grid
                    container
                    spacing={2}
                >

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

            </AccordionDetails>
        </Accordion>
    );
}