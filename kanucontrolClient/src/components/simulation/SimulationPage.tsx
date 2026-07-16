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
    useTheme,
    useMediaQuery,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { radius } from "@/theme/ui";

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

    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("sm"));


    const uebernehmeBeitragsvorschlag = () => {

        if (!ergebnis?.beitragsVorschlag) {
            return;
        }

        setLocalSimulation(prev => {

            if (!prev) {
                return prev;
            }

            return {
                ...prev,
                teilnehmerBeitragUnter21Jahre:
                    ergebnis.beitragsVorschlag.teilnehmerBeitragUnter21Jahre,
                mitarbeiterBeitrag:
                    ergebnis.beitragsVorschlag.mitarbeiterBeitrag,
            };
        });

        setDirty(true);
    };

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
                    position: {
                        xs: "static",
                        md: "sticky",
                    },
                    top: {
                        md: 0,
                    },
                    zIndex: {
                        md: 100,
                    },
                    bgcolor: "background.default",
                    pb: 2,
                    borderRadius: radius.dialog,
                }}
            >
                <SimulationSummary
                    ergebnis={ergebnis}
                    onBeitragsvorschlagUebernehmen={uebernehmeBeitragsvorschlag}
                />
            </Box>

            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="center"
                mb={2}
            >
                <Button
                    variant="outlined"
                    onClick={uebernehmeBeitragsvorschlag}
                >
                    Betragsvorschlag übernehmen
                </Button>

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
                    borderRadius: radius.dialog,
                }}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                >
                    <Typography variant={isMobile ? "h6" : "h5"}>
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
                    borderRadius: radius.dialog,
                }}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                >
                    <Typography variant={isMobile ? "h6" : "h5"}>
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