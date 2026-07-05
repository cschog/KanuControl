import { useParams } from "react-router-dom";

import { useSimulation } from "@/hooks/useSimulation";

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

    if (loading) {
        return <>Lade Simulation…</>;
    }

    if (!simulation || !ergebnis) {
        return <>Keine Daten vorhanden.</>;
    }

    return (

        <>

            <SimulationForm
                simulation={simulation}
                onChange={recalculate}
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