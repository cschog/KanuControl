import React from "react";
import { Routes, Route } from "react-router-dom";

import StartMenue from "@/components/startmenu/StartMenu";
import Vereine from "./components/verein/VereineScreen";
import Personen from "./components/person/PersonenScreen";
import Veranstaltungen from "@/components/veranstaltung/VeranstaltungenScreen";
import TeilnehmerScreen from "@/components/teilnehmer/TeilnehmerScreen";
import Finanzen from "./pages/Finanzen";
import Reisekosten from "./Platzhalter/Reisekosten";
import Teilnehmerliste from "./components/pdfAusgaben/Teilnehmerliste";
import Erhebungsbogen from "./components/pdfAusgaben/Erhebungsbogen";
import VerwaltungPage from "@/components/verwaltung/VerwaltungPage";
import Anmeldung from "./components/pdfAusgaben/Anmeldung";
import Abrechnung from "./components/pdfAusgaben/Abrechnung";
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
          <Route path="/teilnehmerliste" element={<Teilnehmerliste />} />
          <Route path="/erhebungsbogen" element={<Erhebungsbogen />} />
          <Route path="/verwaltung" element={<VerwaltungPage />} />
          <Route path="/anmeldung" element={<Anmeldung />} />
          <Route path="/abrechnung" element={<Abrechnung />} />
          <Route path="/ausgabeReisekosten" element={<AusgabeReisekosten />} />
        </Route>
        <Route path="*" element={<p>Path not resolved</p>} />
      </Routes>
    </div>
  );
};

export default App;
