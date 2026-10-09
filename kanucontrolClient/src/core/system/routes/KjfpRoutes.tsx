import type { RouteObject } from "react-router-dom";

import Vereine from "@/core/components/verein/VereineScreen";
import Personen from "@/core/components/person/PersonenScreen";
import Veranstaltungen from "@/kjfp/components/veranstaltung/VeranstaltungenScreen";
import TeilnehmerScreen from "@/kjfp/components/teilnehmer/TeilnehmerScreen";
import DokumenteScreen from "@/kjfp/components/dokumente/DokumenteScreen";
import VerwaltungPage from "@/kjfp/components/verwaltung/VerwaltungPage";
import AusgabeReisekosten from "@/kjfp/components/pdfAusgaben/AusgabeReisekosten";

import FinanzBereichMenue from "@/kjfp/components/finanzen/FinanzBereichMenue";
import FinanzRoute from "@/kjfp/components/finanzen/FinanzRoute";
import ReisekostenDetailPage from "@/kjfp/components/finanzen/reisekosten/ReisekostenDetailPage";

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
