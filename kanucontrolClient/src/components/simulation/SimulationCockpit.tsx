// src/components/simulation/SimulationCockpit.tsx

import { Grid } from "@mui/material";

import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";

import SimulationForm from "./SimulationForm";
import SimulationSummary from "./SimulationSummary";

interface SimulationCockpitProps {

    simulation: PlanungsSimulation;

    ergebnis: SimulationErgebnis;

    onChange(
        simulation: PlanungsSimulation
    ): void;
}

export default function SimulationCockpit({

    simulation,
    ergebnis,
    onChange,

}: SimulationCockpitProps) {

    return (

        <Grid
            container
            spacing={2}
            sx={{ mb: 2 }}
        >

            <Grid size={{ xs: 12, lg: 8 }}>

                <SimulationForm
                    simulation={simulation}
                    onChange={onChange}
                />

            </Grid>

            <Grid size={{ xs: 12, lg: 4 }}>

                <SimulationSummary
                    ergebnis={ergebnis}
                />

            </Grid>

        </Grid>

    );
}