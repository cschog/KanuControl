import type { RouteObject } from "react-router-dom";

import VermietungStartPage from "@/vermietung/pages/VermietungStart";
import MietobjektePage from "@/vermietung/components/mietobjekte/MietobjektePage";
import BuchungenView from "@/vermietung/pages/BuchungenView";
import VerwaltungPage from "@/vermietung/pages/VerwaltungPage";

const VermietungRoutes: RouteObject[] = [
  {
    path: "/vermietung",
    element: <VermietungStartPage />,
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
    path: "/vermietung/verwaltung",
    element: <VerwaltungPage />,
  },
];

export default VermietungRoutes;
