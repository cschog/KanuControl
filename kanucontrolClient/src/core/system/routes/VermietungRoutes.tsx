import type { RouteObject } from "react-router-dom";

import VermietungStartPage from "@/vermietung/pages/VermietungStart";
import MietobjektePage from "@/vermietung/components/mietobjekte/MietobjektePage";
import BuchungenView from "@/vermietung/pages/BuchungenView";

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
];

export default VermietungRoutes;
