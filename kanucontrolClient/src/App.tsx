import React from "react";
import { Routes, Route } from "react-router-dom";

import StartMenue from "@/components/startmenu/StartMenu";
import Vereine from "./components/verein/Vereine";
import Personen from "./components/person/Personen";
import Veranstaltungen from "@/components/veranstaltung/Veranstaltungen";
import Teilnehmer from "./Platzhalter/Teilnehmer";
import Kosten from "./Platzhalter/Kosten";
import Reisekosten from "./Platzhalter/Reisekosten";
import Teilnehmerliste from "./Platzhalter/Teilnehmerliste";
import Erhebungsbogen from "./Platzhalter/Erhebungsbogen";
import Anmeldung from "./Platzhalter/Anmeldung";
import Abrechnung from "./Platzhalter/Abrechnung";
import AusgabeReisekosten from "./Platzhalter/AusgabeReisekosten";
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
        <Route path="/teilnehmer" element={<Teilnehmer />} />
        <Route path="/kosten" element={<Kosten />} />
        <Route path="/reisekosten" element={<Reisekosten />} />
        <Route path="/teilnehmerliste" element={<Teilnehmerliste />} />
        <Route path="/erhebungsbogen" element={<Erhebungsbogen />} />
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