import type { RouteObject } from "react-router-dom";

import VermietungStartPage from "@/vermietung/pages/VermietungStart";
import MietobjektePage from "@/vermietung/pages/MietobjektePage";
import BuchungenView from "@/vermietung/pages/BuchungenView";
import VerwaltungPage from "@/vermietung/pages/VerwaltungPage";
import MieterPage from "@/vermietung/pages/MieterPage";
import FinanzenPage from "@/vermietung/pages/FinanzenPage";

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
    element: <FinanzenPage />,
  },
  {
    path: "/vermietung/verwaltung",
    element: <VerwaltungPage />,
  },
];

export default VermietungRoutes;
