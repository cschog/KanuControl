// src/components/simulation/SimulationPage.tsx

import { useEffect, useState } from "react";
import { useSimulation } from "@/hooks/useSimulation";
import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import SimulationForm from "./SimulationForm";
import SimulationSummary from "./SimulationSummary";
import PlanungspositionenTable from "./PlanungspositionenTable";
import {
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Box,
    Button,
    Typography,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

interface SimulationPageProps {

    veranstaltungId: number;

}

export default function SimulationPage({
    veranstaltungId,
}: SimulationPageProps) {

    const {
        simulation,
        ergebnis,
        loading,
        error,
        recalculate,
        saveSimulation
    } = useSimulation(veranstaltungId);

    const [localSimulation, setLocalSimulation] =
        useState<PlanungsSimulation>();

    const [dirty, setDirty] = useState(false);

    const [simulationOpen, setSimulationOpen] = useState(true);
    const [positionenOpen, setPositionenOpen] = useState(false);

    useEffect(() => {

        if (!localSimulation && simulation) {
            setLocalSimulation(simulation);
            setDirty(false);
        }

    }, [simulation, localSimulation]);

    useEffect(() => {
        if (!localSimulation) {
            return;
        }

        const timer = setTimeout(() => {
            recalculate(localSimulation);
        }, 300);

        return () => clearTimeout(timer);

    }, [localSimulation, recalculate]);


    if (loading) {
        return <>Lade Simulation…</>;
    }

    if (!localSimulation || !ergebnis) {
        return <>Keine Daten vorhanden.</>;
    }



    return (
        <>
            <Box
                sx={{
                    position: "sticky",
                    top: 0,
                    zIndex: 100,
                    bgcolor: "background.default",
                    pb: 2,
                    borderRadius: 2,
                }}
            >
                <SimulationSummary
                    ergebnis={ergebnis}
                />
            </Box>

            <Box
                display="flex"
                justifyContent="flex-end"
                mb={2}
            >
                <Button
                    variant="contained"
                    disabled={!dirty}
                    onClick={async () => {
                        await saveSimulation(localSimulation);
                        setDirty(false);
                    }}
                >
                    {dirty ? "Simulation speichern" : "Simulation gespeichert"}
                </Button>
            </Box>

            <Accordion
                expanded={simulationOpen}
                onChange={(_, expanded) => setSimulationOpen(expanded)}
                sx={{
                    mt: 2,
                    bgcolor: "grey.100",
                    borderRadius: 2,
                }}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                >
                    <Typography variant="h5">
                        Simulation
                    </Typography>
                </AccordionSummary>

                <AccordionDetails
                    sx={{
                        bgcolor: "grey.200",
                        borderTop: 1,
                        borderColor: "divider",
                    }}
                >
                    <SimulationForm
                        simulation={localSimulation}
                        onChange={(sim) => {
                            setLocalSimulation(sim);
                            setDirty(true);
                        }}
                    />
                </AccordionDetails>
            </Accordion>

            <Accordion
                expanded={positionenOpen}
                onChange={(_, expanded) => setPositionenOpen(expanded)}
                sx={{
                    mt: 2,
                    bgcolor: "grey.100",
                    borderRadius: 2,
                }}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                >
                    <Typography variant="h5">
                        Berechnungspositionen
                    </Typography>
                </AccordionSummary>

                <AccordionDetails
                    sx={{
                        bgcolor: "grey.200",
                        borderTop: 1,
                        borderColor: "divider",
                    }}
                >
                    <PlanungspositionenTable
                        positionen={ergebnis.positionen}
                    />
                </AccordionDetails>
            </Accordion>
        </>
    );
}