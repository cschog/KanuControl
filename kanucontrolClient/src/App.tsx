import React from "react";
import { Routes, Route } from "react-router-dom";

import StartMenue from "@/components/startmenu/StartMenu";
import Vereine from "./components/verein/VereineScreen";
import Personen from "./components/person/PersonenScreen";
import Veranstaltungen from "@/components/veranstaltung/VeranstaltungenScreen";
import TeilnehmerScreen from "@/components/teilnehmer/TeilnehmerScreen";
import DokumenteScreen from "@/components/dokumente/DokumenteScreen";
import VerwaltungPage from "@/components/verwaltung/VerwaltungPage";
import AusgabeReisekosten from "./components/pdfAusgaben/AusgabeReisekosten";

import AppLayout from "@/components/layout/AppLayout";
import FinanzBereichMenue from "@/components/finanzen/FinanzBereichMenue";
import FinanzRoute from "@/components/finanzen/FinanzRoute";

import PostalCodeAdminPage from "@/components/admin/PostalCodeAdminPage";
import AdminPage from "@/components/admin/AdminPage";
import FoerdersatzAdminPage from "@/components/admin/foerdersatz/FoerdersatzAdminPage";
import KikZuschlagAdminPage from "@/components/admin/kik/KikZuschlagAdminPage";
import ReisekostenKonfigurationPage from "@/components/admin/reisekosten/ReisekostenKonfigurationPage";

import ReisekostenDetailPage from "@/components/finanzen/reisekosten/ReisekostenDetailPage";

import ActiveSessionsPage from "@/components/admin/audit/ActiveSessionsPage";
import AuditHistoryPage from "@/components/admin/audit/AuditHistoryPage";
import AuditPage from "@/components/admin/audit/AuditPage";

const App: React.FC = () => {
  return (
    <div className="App">
      <Routes>
        <Route element={<AppLayout />}>
          {/* =====================================================
              START / ALLGEMEINE MODULE
             ===================================================== */}

          <Route path="/" element={<StartMenue />} />
          <Route path="/startmenue" element={<StartMenue />} />

          <Route path="/vereine" element={<Vereine />} />
          <Route path="/personen" element={<Personen />} />
          <Route path="/veranstaltungen" element={<Veranstaltungen />} />
          <Route path="/teilnehmer" element={<TeilnehmerScreen />} />
          <Route path="/dokumente" element={<DokumenteScreen />} />
          <Route path="/verwaltung" element={<VerwaltungPage />} />

          {/* =====================================================
              FINANZEN – NEUE STRUKTUR
             ===================================================== */}

          {/* =====================================================
    FINANZEN
   ===================================================== */}

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/vorbereitung"
            element={
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
            }
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/durchfuehrung"
            element={
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
            }
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/auswertung"
            element={
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
            }
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/simulation"
            element={<FinanzRoute type="simulation" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/planung"
            element={<FinanzRoute type="planung" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/abrechnung"
            element={<FinanzRoute type="abrechnung" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/beitraege"
            element={<FinanzRoute type="beitraege" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/fahrkosten"
            element={<FinanzRoute type="fahrkosten" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/finanzgruppen"
            element={<FinanzRoute type="finanzgruppen" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/dashboard"
            element={<FinanzRoute type="dashboard" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/finanzen/finanzausgleich"
            element={<FinanzRoute type="finanzausgleich" />}
          />

          <Route
            path="/veranstaltungen/:veranstaltungId/reisekosten/:id"
            element={<ReisekostenDetailPage />}
          />

          <Route path="/ausgabeReisekosten" element={<AusgabeReisekosten />} />

          {/* =====================================================
              ADMINISTRATION
             ===================================================== */}

          <Route path="/admin" element={<AdminPage />} />
          <Route path="/admin/postal-codes" element={<PostalCodeAdminPage />} />
          <Route path="/admin/foerdersaetze" element={<FoerdersatzAdminPage />} />
          <Route path="/admin/kik-zuschlaege" element={<KikZuschlagAdminPage />} />
          <Route path="/admin/reisekosten" element={<ReisekostenKonfigurationPage />} />

          <Route path="/admin/audit/active-sessions" element={<ActiveSessionsPage />} />

          <Route path="/admin/audit/history" element={<AuditHistoryPage />} />

          <Route path="/admin/audit" element={<AuditPage />} />
        </Route>
      </Routes>
    </div>
  );
};

export default App;
