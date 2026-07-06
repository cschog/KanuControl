// src/components/simulation/SimulationPage.tsx

import { useEffect, useState } from "react";
import { useSimulation } from "@/hooks/useSimulation";
import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import SimulationForm from "./SimulationForm";
import SimulationSummary from "./SimulationSummary";
import SimulationPositionTable from "./SimulationPositionTable";

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
        recalculate
    } = useSimulation(veranstaltungId);

    const [localSimulation, setLocalSimulation] =
        useState<PlanungsSimulation>();

 useEffect(() => {

    if (!localSimulation && simulation) {
        setLocalSimulation(simulation);
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

            <SimulationForm
                simulation={localSimulation}
                onChange={setLocalSimulation}
            />

            <SimulationSummary
                ergebnis={ergebnis}
            />

            <SimulationPositionTable
                positionen={ergebnis.positionen}
            />

        </>

    );
}