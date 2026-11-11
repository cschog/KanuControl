import { lazy } from "react";
import type { RouteObject } from "react-router-dom";

const VermietungStartPage = lazy(() => import("@/vermietung/pages/VermietungStart"));
const MietobjektePage = lazy(() => import("@/vermietung/pages/MietobjektePage"));
const BuchungenView = lazy(() => import("@/vermietung/pages/BuchungenView"));
const VerwaltungPage = lazy(() => import("@/vermietung/pages/VerwaltungPage"));
const VeranstalterPage = lazy(() => import("@/vermietung/pages/VeranstalterPage"));
const MieterPage = lazy(() => import("@/vermietung/pages/MieterPage"));
const AbrechnungPage = lazy(() => import("@/vermietung/pages/AbrechnungPage"));
const FinanzDashboardPage = lazy(() => import("@/vermietung/pages/FinanzDashboardPage"));
const VerbrauchDashboardPage = lazy(() => import("@/vermietung/pages/VerbrauchDashboardPage"));

const VermietungRoutes: RouteObject[] = [
  { path: "/vermietung/veranstalter", element: <VeranstalterPage /> },
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
