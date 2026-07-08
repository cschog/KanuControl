// src/components/simulation/SimulationForm.tsx

import { useEffect } from "react";
import {
    Card,
    CardContent,
    Divider,
    FormControlLabel,
    Grid,
    MenuItem,
    Switch,
    TextField,
    Typography,
} from "@mui/material";
import InputAdornment from "@mui/material/InputAdornment";

import SliderNumberField from "@/components/common/SliderNumberField";

import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";

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

    console.log("SimulationForm render");

    useEffect(() => {
        console.log("SimulationForm mounted");

        return () => console.log("SimulationForm unmounted");
    }, []);

    const update = <K extends keyof PlanungsSimulation>(
        key: K,
        value: PlanungsSimulation[K]
    ) => {
        onChange({
            ...simulation,
            [key]: value,
        });
    };

    return (
        <Card>
            <CardContent>

                <Typography
                    variant="h6"
                    gutterBottom
                >
                    Simulation
                </Typography>

                <Grid
                    container
                    spacing={2}
                >

                    {/* Allgemein */}

                    <Grid size={12}>
                        <Typography variant="subtitle1">
                            Allgemein
                        </Typography>
                        <Divider />
                    </Grid>

                    <Grid size={{ xs: 12, md: 6 }}>
                        <TextField
                            select
                            fullWidth
                            label="Veranstaltungstyp"
                            value={simulation.typ}
                            onChange={(e) =>
                                update(
                                    "typ",
                                    e.target.value as VeranstaltungTyp
                                )
                            }
                        >
                            {Object.values(VeranstaltungTyp).map((typ) => (
                                <MenuItem
                                    key={typ}
                                    value={typ}
                                >
                                    {typ}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
                        <SliderNumberField
                            label="Teilnehmer"
                            value={simulation.teilnehmer}
                            min={7}
                            max={50}
                            onChange={(value) =>
                                update("teilnehmer", value)
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Mitarbeiter"
                            value={simulation.mitarbeiter}
                            onChange={(e) =>
                                update(
                                    "mitarbeiter",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Tage"
                            value={simulation.tage}
                            onChange={(e) =>
                                update(
                                    "tage",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Nächte"
                            value={simulation.naechte}
                            onChange={(e) =>
                                update(
                                    "naechte",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={simulation.kikZertifiziert}
                                    onChange={(e) =>
                                        update(
                                            "kikZertifiziert",
                                            e.target.checked
                                        )
                                    }
                                />
                            }
                            label="KiK zertifiziert"
                        />
                    </Grid>

                    {/* Preise */}

                    <Grid size={12}>
                        <Typography variant="subtitle1">
                            Preise
                        </Typography>
                        <Divider />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Unterkunft €/Person/Nacht"
                            value={simulation.unterkunftPreisProPersonUndNacht ?? ""}
                            onChange={(e) =>
                                update(
                                    "unterkunftPreisProPersonUndNacht",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Verpflegung €/Person/Tag"
                            value={simulation.verpflegungPreisProPersonUndTag ?? ""}
                            onChange={(e) =>
                                update(
                                    "verpflegungPreisProPersonUndTag",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>
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

                    <Grid size={{ xs: 6, md: 3 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="MA-Beitrag"
                            value={simulation.mitarbeiterBeitrag ?? ""}
                            onChange={(e) =>
                                update(
                                    "mitarbeiterBeitrag",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    {/* Kosten */}

                    <Grid size={12}>
                        <Typography variant="subtitle1">
                            Kosten
                        </Typography>
                        <Divider />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Honorare"
                            value={simulation.honorare ?? ""}
                            onChange={(e) =>
                                update("honorare", Number(e.target.value))
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Fahrtkosten"
                            value={simulation.fahrtkosten ?? ""}
                            onChange={(e) =>
                                update("fahrtkosten", Number(e.target.value))
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Verbrauchsmaterial / Tag"
                            value={simulation.verbrauchsmaterialProTag ?? ""}
                            onChange={(e) =>
                                update(
                                    "verbrauchsmaterialProTag",
                                    Number(e.target.value)
                                )
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Kultur"
                            value={simulation.kultur ?? ""}
                            onChange={(e) =>
                                update("kultur", Number(e.target.value))
                            }
                        />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Miete"
                            value={simulation.miete ?? ""}
                            onChange={(e) =>
                                update("miete", Number(e.target.value))
                            }
                        />
                    </Grid>

                    {/* Einnahmen */}

                    <Grid size={12}>
                        <Typography variant="subtitle1">
                            Einnahmen
                        </Typography>
                        <Divider />
                    </Grid>

                    <Grid size={{ xs: 6, md: 4 }}>
                        <TextField
                            fullWidth
                            type="number"
                            label="Pfand"
                            value={simulation.pfand ?? ""}
                            onChange={(e) =>
                                update("pfand", Number(e.target.value))
                            }
                        />
                    </Grid>

                </Grid>

            </CardContent>
        </Card>
    );
}