import { lazy } from "react";
import type { RouteObject } from "react-router-dom";

const Vereine = lazy(() => import("@/core/components/verein/VereineScreen"));
const Personen = lazy(() => import("@/core/components/person/PersonenScreen"));
const Veranstaltungen = lazy(() => import("@/kjfp/components/veranstaltung/VeranstaltungenScreen"));
const TeilnehmerScreen = lazy(() => import("@/kjfp/components/teilnehmer/TeilnehmerScreen"));
const DokumenteScreen = lazy(() => import("@/kjfp/components/dokumente/DokumenteScreen"));
const VerwaltungPage = lazy(() => import("@/kjfp/components/verwaltung/VerwaltungPage"));
const AusgabeReisekosten = lazy(() => import("@/kjfp/components/pdfAusgaben/AusgabeReisekosten"));
const FinanzBereichMenue = lazy(() => import("@/kjfp/components/finanzen/FinanzBereichMenue"));
const FinanzRoute = lazy(() => import("@/kjfp/components/finanzen/FinanzRoute"));
const ReisekostenDetailPage = lazy(() => import("@/kjfp/components/finanzen/reisekosten/ReisekostenDetailPage"));
const FahrtenPage = lazy(() => import("@/kjfp/components/fahrten/FahrtenPage"));
const FahrtenStartPage = lazy(() => import("@/kjfp/components/fahrten/FahrtenStartPage"));

const KjfpRoutes: RouteObject[] = [
  // =====================================================
  // KJFP – STAMMDATEN
  // =====================================================

  {
    path: "/vereine",
    element: <Vereine />,
  },

  {
    path: "/personen",
    element: <Personen />,
  },

  {
    path: "/veranstaltungen",
    element: <Veranstaltungen />,
  },

  {
    path: "/teilnehmer",
    element: <TeilnehmerScreen />,
  },
  {
    path: "/fahrten",
    element: <FahrtenStartPage />,
  },
  {
    path: "/fahrten/kanus",
    element: <FahrtenPage />,
  },
  {
    path: "/fahrten/gewaesser",
    element: <FahrtenPage />,
  },
  {
    path: "/fahrten/liste",
    element: <FahrtenPage />,
  },

  {
    path: "/dokumente",
    element: <DokumenteScreen />,
  },

  {
    path: "/verwaltung",
    element: <VerwaltungPage />,
  },

  // =====================================================
  // KJFP – FINANZEN
  // =====================================================

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/vorbereitung",
    element: (
      <FinanzBereichMenue
        title="Vorbereitung"
        module={[
          {
            key: "simulation",
            label: "Simulation",
            path: "simulation",
          },
          {
            key: "planung",
            label: "Planung",
            path: "planung",
          },
        ]}
      />
    ),
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/durchfuehrung",
    element: (
      <FinanzBereichMenue
        title="Durchführung"
        module={[
          {
            key: "beitraege",
            label: "Beiträge",
            path: "beitraege",
          },
          {
            key: "abrechnung",
            label: "Abrechnung",
            path: "abrechnung",
          },
          {
            key: "fahrkosten",
            label: "Fahrkosten",
            path: "fahrkosten",
          },
          {
            key: "finanzgruppen",
            label: "Konten",
            path: "finanzgruppen",
          },
        ]}
      />
    ),
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/auswertung",
    element: (
      <FinanzBereichMenue
        title="Auswertung"
        module={[
          {
            key: "dashboard",
            label: "Dashboard",
            path: "dashboard",
          },
          {
            key: "finanzausgleich",
            label: "Finanzausgleich",
            path: "finanzausgleich",
          },
        ]}
      />
    ),
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/simulation",
    element: <FinanzRoute type="simulation" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/planung",
    element: <FinanzRoute type="planung" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/abrechnung",
    element: <FinanzRoute type="abrechnung" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/beitraege",
    element: <FinanzRoute type="beitraege" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/fahrkosten",
    element: <FinanzRoute type="fahrkosten" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/finanzgruppen",
    element: <FinanzRoute type="finanzgruppen" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/dashboard",
    element: <FinanzRoute type="dashboard" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/finanzen/finanzausgleich",
    element: <FinanzRoute type="finanzausgleich" />,
  },

  {
    path: "/veranstaltungen/:veranstaltungId/reisekosten/:id",
    element: <ReisekostenDetailPage />,
  },

  {
    path: "/ausgabeReisekosten",
    element: <AusgabeReisekosten />,
  },
];

export default KjfpRoutes;
