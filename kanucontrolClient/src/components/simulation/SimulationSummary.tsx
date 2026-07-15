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
} from "@mui/material";

interface Props {
    ergebnis: SimulationErgebnis;
    onBeitragsvorschlagUebernehmen: () => void;
}

export default function SimulationSummary({ ergebnis, onBeitragsvorschlagUebernehmen }: Props) {

    const zuschuss = ergebnis.positionen
        .filter(p => p.kategorie === "KJFP_ZUSCHUSS")
        .reduce((s, p) => s + p.betrag, 0);


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

                    <Divider sx={{ my: 2 }} />

                    <Typography variant="subtitle2" gutterBottom>
                        Beitragsvorschlag
                    </Typography>

                    <Grid container spacing={2}>

                        <Grid size={{ xs: 12, sm: 4 }}>
                            <Typography variant="body2" color="text.secondary">
                                Teilnehmer U21
                            </Typography>
                            <Money
                                value={
                                    ergebnis.beitragsVorschlag
                                        .teilnehmerBeitragUnter21Jahre
                                }
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 4 }}>
                            <Typography variant="body2" color="text.secondary">
                                Mitarbeiter
                            </Typography>
                            <Money
                                value={
                                    ergebnis.beitragsVorschlag
                                        .mitarbeiterBeitrag
                                }
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 4 }}>
                            <Typography variant="body2" color="text.secondary">
                                Ergebnis
                            </Typography>
                            <Money
                                value={
                                    ergebnis.beitragsVorschlag
                                        .durchschnittlicherPersonenbeitrag
                                }
                                sx={{ fontWeight: 700 }}
                            />
                        </Grid>

                    </Grid>
                </Paper>
            </Box>
        </>
    );
}