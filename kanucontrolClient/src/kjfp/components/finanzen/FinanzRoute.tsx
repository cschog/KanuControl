import { lazy } from "react";
import { Alert } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";

const SimulationPage = lazy(() => import("@/kjfp/components/simulation/SimulationPage"));
const PlanungPage = lazy(() => import("@/kjfp/components/finanzen/planung/PlanungPage"));
const BuchungenPage = lazy(() => import("@/kjfp/components/finanzen/abrechnung/BuchungenPage"));
const BeitraegePage = lazy(() => import("@/kjfp/components/finanzen/beitraege/BeitraegePage"));
const ReisekostenPage = lazy(() => import("@/kjfp/components/finanzen/reisekosten/ReisekostenPage"));
const KontoPage = lazy(() => import("@/kjfp/components/finanzen/konto/KontoPage"));
const FinanzenDashboard = lazy(() => import("@/kjfp/components/finanzen/FinanzenDashboard"));
const FinanzausgleichPage = lazy(() => import("@/kjfp/components/finanzen/finanzausgleich/FinanzausgleichPage"));

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
      return <FinanzausgleichPage veranstaltungId={id} />;

    default:
      return null;
  }
};

export default FinanzRoute;
