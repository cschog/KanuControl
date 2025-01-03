import React from "react";
import Navigation from "./components/Nav";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import StartMenue from "./components/StartMenue";
import Vereine from "./components/Vereine/Vereine";
import Personen from "./components/Mitglieder/Personen";
import Veranstaltungen from "./Platzhalter/Veranstaltungen";
import Teilnehmer from "./Platzhalter/Teilnehmer";
import Kosten from "./Platzhalter/Kosten";
import Reisekosten from "./Platzhalter/Reisekosten";
import Teilnehmerliste from "./Platzhalter/Teilnehmerliste";
import Erhebungsbogen from "./Platzhalter/Erhebungsbogen";
import Anmeldung from "./Platzhalter/Anmeldung";
import Abrechnung from "./Platzhalter/Abrechnung";
import AusgabeReisekosten from "./Platzhalter/AusgabeReisekosten";

const App: React.FC = () => {

  return (
    <div className="App">
      <Navigation />
     
      <Router>
        <Routes>
          <Route path="/startmenue" element={<StartMenue />} />
          <Route path="/" element={<StartMenue />} />
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
          <Route path="*" element={<p>Path not resolved</p>} />
        </Routes>
      </Router>
    </div>
  );
};

export default App;