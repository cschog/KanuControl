import FinanzSummary from "@/components/common/FinanzSummary";
import Money from "@/components/common/Money";
import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";

import {
    Box,
    Button,
    Grid,
    Paper,
    Typography,
    Divider,
    useTheme,
    useMediaQuery,
} from "@mui/material";


interface Props {
    ergebnis: SimulationErgebnis;
    onBeitragsvorschlagUebernehmen: () => void;
}

interface MobileValueRowProps {
    label: string;
    value: number;
    bold?: boolean;
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
            <Typography
                variant="body2"
                color="text.secondary"
            >
                {label}
            </Typography>

            <Money
                value={value}
                variant="body2"
                sx={{
                    fontWeight: bold ? 700 : 500,
                }}
            />
        </Box>
    );
}

export default function SimulationSummary({ ergebnis, onBeitragsvorschlagUebernehmen }: Props) {

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
                <Paper variant="outlined" sx={{ p: 2 }}>
                    <Typography variant="subtitle1" gutterBottom>
                        Teilnehmerbeiträge
                    </Typography>

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
                                <Typography variant="body2" color="text.secondary">
                                    Summe Beiträge
                                </Typography>
                                <Money value={ergebnis.summeTeilnehmerbeitraege} />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 4 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Ø Personenbeitrag
                                </Typography>
                                <Money value={ergebnis.durchschnittlicherPersonenbeitrag} />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 4 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Empfohlener Ø-Beitrag
                                </Typography>
                                <Money
                                    value={ergebnis.empfohlenerPersonenbeitrag}
                                    sx={{ fontWeight: 700 }}
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
                                <Typography variant="body2" color="text.secondary">
                                    Teilnehmer U21
                                </Typography>
                                <Money
                                    value={ergebnis.beitragsVorschlag.teilnehmerBeitragUnter21Jahre}
                                />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 4 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Mitarbeiter
                                </Typography>
                                <Money
                                    value={ergebnis.beitragsVorschlag.mitarbeiterBeitrag}
                                />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 4 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Ergebnis
                                </Typography>
                                <Money
                                    value={ergebnis.beitragsVorschlag.durchschnittlicherPersonenbeitrag}
                                    sx={{ fontWeight: 700 }}
                                />
                            </Grid>
                        </Grid>
                    )}

                </Paper>

            </Box>

        </>
    );
}