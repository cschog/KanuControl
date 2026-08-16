import { Alert } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";

import SimulationPage from "@/components/simulation/SimulationPage";
import PlanungPage from "@/components/finanzen/planung/PlanungPage";
import BuchungenPage from "@/components/finanzen/abrechnung/BuchungenPage";
import BeitraegePage from "@/components/finanzen/beitraege/BeitraegePage";
import ReisekostenPage from "@/components/finanzen/reisekosten/ReisekostenPage";
import KontoPage from "@/components/finanzen/KontoPage";
import FinanzenDashboard from "@/components/finanzen/FinanzenDashboard";

// später:
// import FinanzausgleichPage from "...";

interface Props {
  type:
    | "simulation"
    | "planung"
    | "abrechnung"
    | "beitraege"
    | "fahrkosten"
    | "finanzgruppen"
    | "dashboard"
    | "finanzausgleich";
}

const FinanzRoute = ({ type }: Props) => {
  const { veranstaltungId } = useParams<{ veranstaltungId: string }>();
  const navigate = useNavigate();

  if (!veranstaltungId) {
    return null;
  }

  const id = Number(veranstaltungId);

  if (Number.isNaN(id)) {
    return <Alert severity="error">Ungültige Veranstaltungs-ID</Alert>;
  }

  switch (type) {
    case "simulation":
      return <SimulationPage veranstaltungId={id} />;

    case "planung":
      return (
        <PlanungPage
          veranstaltungId={id}
          onOpenSimulation={() => navigate(`/veranstaltungen/${id}/finanzen/simulation`)}
        />
      );

    case "abrechnung":
      return <BuchungenPage veranstaltungId={id} />;

    case "beitraege":
      return <BeitraegePage veranstaltungId={id} />;

    case "fahrkosten":
      return <ReisekostenPage veranstaltungId={id} />;

    case "finanzgruppen":
      return <KontoPage veranstaltungId={id} />;

    case "dashboard":
      return <FinanzenDashboard />;

    case "finanzausgleich":
      return <Alert severity="info">Finanzausgleich ist noch nicht implementiert.</Alert>;

    default:
      return null;
  }
};

export default FinanzRoute;
