import {
    Card,
    CardContent,
    FormControlLabel,
    Grid,
    MenuItem,
    Switch,
    TextField,
    Typography,
} from "@mui/material";

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

                    <Grid size={{ xs: 12, md: 6 }}>

                        <TextField
                            select
                            fullWidth
                            label="Veranstaltungstyp"
                            value={simulation.typ || ""}
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

                        <TextField
                            fullWidth
                            type="number"
                            label="Teilnehmer"
                            value={simulation.teilnehmer}
                            onChange={(e) =>
                                update(
                                    "teilnehmer",
                                    Number(e.target.value)
                                )
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

                        <TextField
                            fullWidth
                            type="number"
                            label="Unterkunft €/Nacht"
                            value={
                                simulation.unterkunftPreisProPersonUndNacht ?? ""
                            }
                            onChange={(e) =>
                                update(
                                    "unterkunftPreisProPersonUndNacht",
                                    Number(e.target.value)
                                )
                            }
                        />

                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>

                        <TextField
                            fullWidth
                            type="number"
                            label="Verpflegung €/Tag"
                            value={
                                simulation.verpflegungPreisProPersonUndTag ?? ""
                            }
                            onChange={(e) =>
                                update(
                                    "verpflegungPreisProPersonUndTag",
                                    Number(e.target.value)
                                )
                            }
                        />

                    </Grid>

                    <Grid size={{ xs: 6, md: 3 }}>

                        <TextField
                            fullWidth
                            type="number"
                            label="Teilnehmerbeitrag"
                            value={
                                simulation.standardGebuehr ?? ""
                            }
                            onChange={(e) =>
                                update(
                                    "standardGebuehr",
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

                </Grid>

            </CardContent>

        </Card>
    );
}