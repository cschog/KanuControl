import React, { useCallback } from "react";
import { Button } from "primereact/button";

const StartMenue = () => {
  const callVereine = useCallback(function () {
    window.location.href = "/vereine";
  }, []);

  const callMitglieder = useCallback(function () {
    window.location.href = "/personen";
  }, []);

  const callVeranstaltungen = useCallback(function () {
    window.location.href = "/veranstaltungen";
  }, []);

  const callTeilnehmer = useCallback(function () {
    window.location.href = "/teilnehmer";
  }, []);

  const callKosten = useCallback(function () {
    window.location.href = "/kosten";
  }, []);

  const callReisekosten = useCallback(function () {
    window.location.href = "/reisekosten";
  }, []);

  const callTeilnehmerliste = useCallback(function () {
    window.location.href = "/teilnehmerliste";
  }, []);

  const callErhebungsbogen = useCallback(function () {
    window.location.href = "/erhebungsbogen";
  }, []);

  const callAnmeldung = useCallback(function () {
    window.location.href = "/anmeldung";
  }, []);

  const callAbrechnung = useCallback(function () {
    window.location.href = "/abrechnung";
  }, []);

  const callAusgabeReisekosten = useCallback(function () {
    window.location.href = "/ausgabeReisekosten";
  }, []);

  return (
    <div>
      <h1>KanuControl</h1>
      <h4>aktive Veranstaltung</h4>
      <p>Test-Veranstaltung</p>
      <div className="grid border-solid m-auto w-11">
        <div className="col">
          <div className="p-3">
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Vereine"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callVereine}></Button>
            </div>
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Mitglieder"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callMitglieder}></Button>
            </div>
          </div>
        </div>
        <div className="col">
          <div className="p-3">
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Veranstaltungen"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callVeranstaltungen}></Button>
            </div>
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Teilnehmer"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callTeilnehmer}></Button>
            </div>
          </div>
        </div>
        <div className="col">
          <div className="p-3">
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Kosten"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callKosten}></Button>
            </div>
            <div className="block bg-blue-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Reisekosten"
                className="w-full bg-blue-700 hover:text-yellow-500"
                onClick={callReisekosten}></Button>
            </div>
          </div>
        </div>
      </div>
      <p></p>

      {/* Ab hier kommt das Menue zur Abrechnung */}

      <div className="grid border-solid m-auto w-11">
        <div className="col">
          <div className="p-3">
            <div className="block bg-yellow-500 font-bold text-center p-3 border-round mb-3">
              <Button
                label="Teilnehmerliste"
                className="w-full text-900 hover:text-pink-500 bg-yellow-500"
                onClick={callTeilnehmerliste}></Button>
            </div>
            <div className="block bg-yellow-500 font-bold text-center p-3 border-round mb-3">
              <Button
                label="Erhebungsbogen"
                className="w-full text-900 hover:text-pink-500 bg-yellow-500"
                onClick={callErhebungsbogen}></Button>
            </div>
          </div>
        </div>
        <div className="col">
          <div className="p-3">
            <div className="block bg-red-700 text-white font-bold text-center p-3 border-round mb-3">
              <Button
                label="Deckblatt Anmeldung"
                className="w-full text-50 hover:text-yellow-500 bg-red-700"
                onClick={callAnmeldung}></Button>
            </div>
            <div className="block bg-yellow-500 font-bold text-center p-3 border-round mb-3">
              <Button
                label="Deckblatt Abrechnung"
                className="w-full text-900 hover:text-pink-500 bg-yellow-500"
                onClick={callAbrechnung}></Button>
            </div>
          </div>
        </div>
        <div className="col">
          <div className="p-3">
            <div className="block bg-yellow-500 font-bold text-center p-3 border-round mb-3">
              <Button
                label="Ausgabe Reisekosten"
                className="w-full text-900 hover:text-pink-500 bg-yellow-500"
                onClick={callAusgabeReisekosten}></Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StartMenue;
