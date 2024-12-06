import React from "react";
import "./App.css";
import "primereact/resources/themes/md-light-indigo/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "primeflex/primeflex.css";
import Navigation from "./components/Nav";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./components/Login";
import StartMenue from "./components/StartMenue";
import Vereine from "./components/Vereine/Vereine";
import Personen from "./components/Mitglieder/Personen";
import Veranstaltungen from "./components/Veranstaltungen";
import Teilnehmer from "./components/Teilnehmer";
import Kosten from "./components/Kosten";
import Reisekosten from "./components/Reisekosten";
import Teilnehmerliste from "./components/Teilnehmerliste";
import Erhebungsbogen from "./components/Erhebungsbogen";
import Anmeldung from "./components/Anmeldung";
import Abrechnung from "./components/Abrechnung";
import AusgabeReisekosten from "./components/AusgabeReisekosten";

const App = () => {
  return (
    <div className="App">
      <Navigation />
      <Router>
        <Routes>
          <Route
            path="/login"
            element={<Login />}
          />
          <Route
            path="/startmenue"
            element={<StartMenue />}
          />
          <Route
            path="/"
            element={null}
          />
          <Route
            path="/vereine"
            element={<Vereine />}
          />
          <Route
            path="/personen"
            element={<Personen />}
          />
          <Route
            path="/veranstaltungen"
            element={<Veranstaltungen />}
          />
          <Route
            path="/teilnehmer"
            element={<Teilnehmer />}
          />
          <Route
            path="/kosten"
            element={<Kosten />}
          />
          <Route
            path="/reisekosten"
            element={<Reisekosten />}
          />
          <Route
            path="/teilnehmerliste"
            element={<Teilnehmerliste />}
          />
          <Route
            path="/erhebungsbogen"
            element={<Erhebungsbogen />}
          />
          <Route
            path="/anmeldung"
            element={<Anmeldung />}
          />
          <Route
            path="/abrechnung"
            element={<Abrechnung />}
          />
          <Route
            path="/ausgabeReisekosten"
            element={<AusgabeReisekosten />}
          />
          <Route path="*" element={<p>Path not resolved</p>} />
        </Routes>
      </Router>
    </div>
  );
};

export default App;
