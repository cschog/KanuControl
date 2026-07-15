// src/components/simulation/SimulationForm.tsx

import { useEffect } from "react";
import {
    Card,
    CardContent,
    Box,
    Grid,
    Paper,
    Typography,
    FormControlLabel,
    Switch,
} from "@mui/material";

import PaidIcon from "@mui/icons-material/Paid";
import SavingsIcon from "@mui/icons-material/Savings";
import SellIcon from "@mui/icons-material/Sell";
import GroupsIcon from "@mui/icons-material/Groups";

import SimulationSection
    from "@/components/simulation/SimulationSection";

import SliderNumberField from "@/components/common/SliderNumberField";
import RahmendatenAccordion
    from "@/components/simulation/RahmendatenAccordion";
import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";

interface SimulationFormProps {
    simulation: PlanungsSimulation;

    onChange(
        simulation: PlanungsSimulation
    ): void;
}

export default function SimulationForm({
    simulation,
    onChange,
}: SimulationFormProps) {

    const fieldSize = {
        xs: 12, // Handy: 1 Spalte
        sm: 6,  // Tablet: 2 Spalten
        md: 6,  // kleines Notebook: 2 Spalten
        lg: 3,  // Desktop: 4 Spalten
        xl: 3,  // großer Desktop: 4 Spalten
    };

    const sectionStyle = {
        p: 2,
        borderRadius: 2,
    };

    const update = <K extends keyof PlanungsSimulation>(
        key: K,
        value: PlanungsSimulation[K]
    ) => {

        const next = {
            ...simulation,
            [key]: value,
        };

        if (key === "teilnehmer") {
            next.mitarbeiter = Math.ceil(Number(value) / 5);
        }

        onChange(next);
    };

    return (
        <Card>
            <CardContent>

                <Grid
                    container
                    spacing={2}
                >

                    {/* Allgemein */}
                    <Grid size={12}>
                        <RahmendatenAccordion
                            veranstaltung={simulation.veranstaltung}
                        />
                    </Grid>

                    {!simulation.veranstaltung.vereinKikZertifiziert && (

                        // Schalter anzeigen
                        <Grid size={12}>
                            <Paper
                                variant="outlined"
                                sx={{
                                    p: 2,
                                    display: "flex",
                                    alignItems: "center",
                                }}
                            >
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={simulation.kikZertifiziert}
                                            onChange={(e) =>
                                                update("kikZertifiziert", e.target.checked)
                                            }
                                        />
                                    }
                                    label="KiK-Zertifikat für die Veranstaltung vorhanden"
                                />
                            </Paper>
                        </Grid>
                    )}
                    
                    <Grid size={12}>
                        <Typography
                            variant="h6"
                            sx={{ mt: 3 }}
                        >
                            Simulationsparameter
                        </Typography>
                    </Grid>

                    <SimulationSection title="Teilnehmer" icon={<GroupsIcon color="primary"
                        fontSize="small" />}>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Teilnehmer"
                                value={simulation.teilnehmer}
                                min={7}
                                max={100}
                                step={1}
                                marks
                                onChange={(value) => update("teilnehmer", value)}
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <Paper
                                variant="outlined"
                                sx={{
                                    p: 1.5,
                                    bgcolor: "grey.100",
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 1.5,
                                    height: "100%",
                                }}
                            >
                                <GroupsIcon color="action" />

                                <Box>
                                    <Typography variant="body2" fontWeight={600}>
                                        {simulation.mitarbeiter} Mitarbeiter
                                    </Typography>

                                    <Typography variant="caption" color="text.secondary">
                                        automatisch berechnet
                                    </Typography>
                                </Box>
                            </Paper>
                        </Grid>

                    </SimulationSection>


                    <SimulationSection
                        title="Preise und Beiträge"
                        icon={<SellIcon color="primary"
                            fontSize="small" />}
                    >

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Unterkunft €/Person/Nacht"
                                value={simulation.unterkunftPreisProPersonUndNacht ?? 0}
                                min={0}
                                max={100}
                                step={1}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "unterkunftPreisProPersonUndNacht",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Verpflegung €/Person/Tag"
                                value={simulation.verpflegungPreisProPersonUndTag ?? 0}
                                min={0}
                                max={50}
                                step={0.5}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "verpflegungPreisProPersonUndTag",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="TN-Beitrag (<21 Jahre)"
                                value={simulation.teilnehmerBeitragUnter21Jahre ?? 0}
                                min={0}
                                max={300}
                                step={5}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "teilnehmerBeitragUnter21Jahre",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="MA-Beitrag"
                                value={simulation.mitarbeiterBeitrag ?? 0}
                                min={0}
                                max={600}
                                step={5}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "mitarbeiterBeitrag",
                                        value
                                    )
                                }
                            />
                        </Grid>

                    </SimulationSection>

                    <SimulationSection
                        title="Kosten"
                        icon={
                            <PaidIcon
                                color="primary"
                                fontSize="small"
                            />
                        }
                    >

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Fahrtkosten"
                                value={simulation.fahrtkosten ?? 0}
                                min={0}
                                max={8000}
                                step={25}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "fahrtkosten",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Verbrauchsmaterial / Tag"
                                value={simulation.verbrauchsmaterialProTag ?? 0}
                                min={0}
                                max={1000}
                                step={5}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "verbrauchsmaterialProTag",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Honorare"
                                value={simulation.honorare ?? 0}
                                min={0}
                                max={500}
                                step={25}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "honorare",
                                        value
                                    )
                                }
                            />
                        </Grid>


                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Kultur"
                                value={simulation.kultur ?? 0}
                                min={0}
                                max={500}
                                step={25}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "kultur",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Miete"
                                value={simulation.miete ?? 0}
                                min={0}
                                max={2000}
                                step={50}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "miete",
                                        value
                                    )
                                }
                            />
                        </Grid>

                        <Grid size={fieldSize}>
                            <SliderNumberField
                                label="Sonstige Kosten / Tag"
                                value={simulation.sonstigeKostenProTag ?? 0}
                                min={0}
                                max={100}
                                step={5}
                                suffix="€"
                                onChange={(value) =>
                                    update(
                                        "sonstigeKostenProTag",
                                        value
                                    )
                                }
                            />
                        </Grid>

                    </SimulationSection>

                </Grid>

            </CardContent>
        </Card >
    );
}