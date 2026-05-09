import React from "react";
import { Routes, Route } from "react-router-dom";

import StartMenue from "@/components/startmenu/StartMenu";
import Vereine from "./components/verein/VereineScreen";
import Personen from "./components/person/PersonenScreen";
import Veranstaltungen from "@/components/veranstaltung/VeranstaltungenScreen";
import TeilnehmerScreen from "@/components/teilnehmer/TeilnehmerScreen";
import Finanzen from "./components/finanzen/FinanzenScreen";
import Reisekosten from "./Platzhalter/Reisekosten";
import DokumenteScreen from "@/components/dokumente/DokumenteScreen";
import VerwaltungPage from "@/components/verwaltung/VerwaltungPage";
import AusgabeReisekosten from "./components/pdfAusgaben/AusgabeReisekosten";
import AppLayout from "@/components/layout/AppLayout";

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
          <Route path="/reisekosten" element={<Reisekosten />} />
          <Route path="/verwaltung" element={<VerwaltungPage />} />
          <Route path="/dokumente" element={<DokumenteScreen />} />
          <Route path="/ausgabeReisekosten" element={<AusgabeReisekosten />} />
        </Route>
        <Route path="*" element={<p>Path not resolved</p>} />
      </Routes>
    </div>
  );
};

export default App;
