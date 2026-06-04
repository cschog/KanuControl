import React from "react";
import { Routes, Route } from "react-router-dom";

import StartMenue from "@/components/startmenu/StartMenu";
import Vereine from "./components/verein/VereineScreen";
import Personen from "./components/person/PersonenScreen";
import Veranstaltungen from "@/components/veranstaltung/VeranstaltungenScreen";
import TeilnehmerScreen from "@/components/teilnehmer/TeilnehmerScreen";
import Finanzen from "./components/finanzen/FinanzenScreen";
import DokumenteScreen from "@/components/dokumente/DokumenteScreen";
import VerwaltungPage from "@/components/verwaltung/VerwaltungPage";
import AusgabeReisekosten from "./components/pdfAusgaben/AusgabeReisekosten";
import AppLayout from "@/components/layout/AppLayout";
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
          <Route path="/" element={<StartMenue />} />
          <Route path="/startmenue" element={<StartMenue />} />
          <Route path="/vereine" element={<Vereine />} />
          <Route path="/personen" element={<Personen />} />
          <Route path="/veranstaltungen" element={<Veranstaltungen />} />
          <Route path="/teilnehmer" element={<TeilnehmerScreen />} />
          <Route path="/veranstaltungen/:veranstaltungId/finanzen" element={<Finanzen />} />
          <Route
            path="/veranstaltungen/:veranstaltungId/reisekosten/:id"
            element={<ReisekostenDetailPage />}
          />
          <Route path="/verwaltung" element={<VerwaltungPage />} />
          <Route path="/dokumente" element={<DokumenteScreen />} />
          <Route path="/ausgabeReisekosten" element={<AusgabeReisekosten />} />
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
