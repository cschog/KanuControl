import FinanzSummary from "@/components/common/FinanzSummary";
import Money from "@/components/common/Money";
import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";
import {
    fontSize,
} from "@/theme/ui";
import {
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Box,
    Grid,
    Typography,
    Divider,
    useTheme,
    useMediaQuery,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { PropsWithChildren } from "react";

interface Props {
    ergebnis: SimulationErgebnis;
    onBeitragsvorschlagUebernehmen: () => void;
}

interface MobileValueRowProps {
    label: string;
    value: number;
    bold?: boolean;
}

function ValueLabel({ children }: PropsWithChildren) {
    return (
        <Typography
            variant="body2"
            color="text.secondary"
            sx={{
                fontSize: fontSize.fieldLabel,
                fontWeight: 500,
            }}
        >
            {children}
        </Typography>
    );
}

function MobileValueRow({
    label,
    value,
    bold = false,
}: MobileValueRowProps) {
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
            <ValueLabel>{label}</ValueLabel>

            <Money
                value={value}
                variant="body2"
                sx={{
                    fontWeight: bold ? 700 : 500, fontSize: fontSize.sectionTitle,
                }}
            />
        </Box>
    );
}

export default function SimulationSummary({ ergebnis }: Props) {

    const zuschuss = ergebnis.positionen
        .filter(p => p.kategorie === "KJFP_ZUSCHUSS")
        .reduce((s, p) => s + p.betrag, 0);

    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

    return (
        <>
            <FinanzSummary
                kosten={ergebnis.kosten}
                einnahmen={ergebnis.einnahmen}
                eigenanteil={ergebnis.saldo}
                kjfpZuschuss={zuschuss}
            />

            <Box mt={2}>
                <Accordion defaultExpanded>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Box
                            display="flex"
                            justifyContent="space-between"
                            alignItems="center"
                            width="100%"
                            pr={2}
                        >
                            <Typography
                                variant="subtitle1"
                                sx={{
                                    fontSize: fontSize.accordionTitle,
                                }}
                            >
                                Teilnehmerbeiträge
                            </Typography>

                            <Money
                                value={ergebnis.empfohlenerPersonenbeitrag}
                                sx={{
                                    fontWeight: 700,
                                    fontSize: fontSize.sectionTitle,
                                }}
                            />
                        </Box>
                    </AccordionSummary>
                    <AccordionDetails>
                        {isMobile ? (
                            <>
                                <MobileValueRow
                                    label="Summe Beiträge"
                                    value={ergebnis.summeTeilnehmerbeitraege}
                                />
                                <MobileValueRow
                                    label="Ø Personenbeitrag"
                                    value={ergebnis.durchschnittlicherPersonenbeitrag}
                                />
                                <MobileValueRow
                                    label="Empfohlener Ø-Beitrag"
                                    value={ergebnis.empfohlenerPersonenbeitrag}
                                    bold
                                />
                            </>
                        ) : (

                            <Grid container spacing={2}>
                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Summe Beiträge</ValueLabel>
                                    <Money value={ergebnis.summeTeilnehmerbeitraege} />
                                </Grid>

                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Ø Personenbeitrag</ValueLabel>
                                    <Money value={ergebnis.durchschnittlicherPersonenbeitrag} />
                                </Grid>

                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Empfohlener Ø-Beitrag</ValueLabel>
                                    <Money
                                        value={ergebnis.empfohlenerPersonenbeitrag}
                                        sx={{
                                            fontWeight: 700,
                                            fontSize: fontSize.sectionTitle,

                                        }}
                                    />
                                </Grid>
                            </Grid>
                        )}
                        <Divider sx={{ my: 2 }} />

                        <Typography variant="subtitle2" gutterBottom>
                            Beitragsvorschlag
                        </Typography>

                        {isMobile ? (
                            <>
                                <MobileValueRow
                                    label="Teilnehmer U21"
                                    value={ergebnis.beitragsVorschlag.teilnehmerBeitragUnter21Jahre}
                                />

                                <MobileValueRow
                                    label="Mitarbeiter"
                                    value={ergebnis.beitragsVorschlag.mitarbeiterBeitrag}
                                />

                                <MobileValueRow
                                    label="Ergebnis"
                                    value={ergebnis.beitragsVorschlag.durchschnittlicherPersonenbeitrag}
                                    bold
                                />
                            </>
                        ) : (
                            <Grid container spacing={2}>
                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Teilnehmer U21</ValueLabel>
                                    <Money
                                        value={ergebnis.beitragsVorschlag.teilnehmerBeitragUnter21Jahre}
                                    />
                                </Grid>

                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Mitarbeiter</ValueLabel>
                                    <Money
                                        value={ergebnis.beitragsVorschlag.mitarbeiterBeitrag}
                                    />
                                </Grid>

                                <Grid size={{ xs: 12, sm: 4 }}>
                                    <ValueLabel>Ergebnis</ValueLabel>
                                    <Money
                                        value={ergebnis.beitragsVorschlag.durchschnittlicherPersonenbeitrag}
                                        sx={{
                                            fontWeight: 700,
                                            fontSize: fontSize.sectionTitle,
                                        }}
                                    />
                                </Grid>
                            </Grid>
                        )}
                    </AccordionDetails>
                </Accordion>

            </Box>

        </>
    );
}