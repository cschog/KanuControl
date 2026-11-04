import type { RouteObject } from "react-router-dom";

import VermietungStartPage from "@/vermietung/pages/VermietungStart";
import MietobjektePage from "@/vermietung/pages/MietobjektePage";
import BuchungenView from "@/vermietung/pages/BuchungenView";
import VerwaltungPage from "@/vermietung/pages/VerwaltungPage";
import MieterPage from "@/vermietung/pages/MieterPage";
import AbrechnungPage from "@/vermietung/pages/AbrechnungPage";
import FinanzDashboardPage from "@/vermietung/pages/FinanzDashboardPage";

import VerbrauchDashboardPage from "@/vermietung/pages/VerbrauchDashboardPage";

const VermietungRoutes: RouteObject[] = [
  {
    path: "/vermietung",
    element: <VermietungStartPage />,
  },
  {
    path: "/vermietung/mieter",
    element: <MieterPage />,
  },
  {
    path: "/vermietung/mietobjekte",
    element: <MietobjektePage />,
  },
  {
    path: "/vermietung/buchung",
    element: <BuchungenView />,
  },
  {
    path: "/vermietung/finanzen",
    element: <AbrechnungPage />,
  },
  { path: "/vermietung/abrechnung", element: <AbrechnungPage /> },
  { path: "/vermietung/verbrauch-dashboard", element: <VerbrauchDashboardPage /> },
  { path: "/vermietung/finanz-dashboard", element: <FinanzDashboardPage /> },
  {
    path: "/vermietung/verwaltung",
    element: <VerwaltungPage />,
  },
];

export default VermietungRoutes;
