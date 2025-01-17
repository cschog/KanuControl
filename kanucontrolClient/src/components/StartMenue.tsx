import { useEffect, useState } from "react";
import { Button } from "primereact/button";
import apiClient from "../services/apiClient"; // Import the configured axios instance
import "primereact/resources/themes/md-light-indigo/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";

const StartMenue = () => {
  const [activeSchema, setActiveSchema] = useState("");
  const [error, setError] = useState(null);

  // Fetch the active schema name on component mount
  useEffect(() => {
    const fetchSchema = async () => {
      try {
        const response = await apiClient.get("/active-schema"); // Use apiClient to fetch the schema
        setActiveSchema(response.data); // Assume API returns the schema name in plain text or JSON
      } catch (err) {
        console.error("Error fetching active schema:", err);
        setError("Failed to fetch the active schema.");
      }
    };
    fetchSchema();
  }, []);
  const callVereine = () => (window.location.href = "/vereine");
  const callMitglieder = () => (window.location.href = "/personen");
  const callVeranstaltungen = () => (window.location.href = "/veranstaltungen");
  const callTeilnehmer = () => (window.location.href = "/teilnehmer");
  const callKosten = () => (window.location.href = "/kosten");
  const callReisekosten = () => (window.location.href = "/reisekosten");
  const callAnmeldung = () => (window.location.href = "/anmeldung");
  const callAbrechnung = () => (window.location.href = "/abrechnung");
  const callTeilnehmerliste = () => (window.location.href = "/teilnehmerliste");
  const callAusgabeReisekosten = () => (window.location.href = "/ausgabeReisekosten");
  const callErhebungsbogen = () => (window.location.href = "/erhebungsbogen");

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">KanuControl</h1>
      <h4 className="text-lg mb-2">Mandant:</h4>
      <p className="mb-6">
        {error ? (
          <span className="text-red-500">{error}</span>
        ) : activeSchema ? (
          activeSchema
        ) : (
          "Lädt..."
        )}
      </p>

      {/* Single grid block with 3 columns in large screens */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {/* 1) Vereine */}
        <Button
          label="Vereine"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callVereine}
        />

        {/* 2) Mitglieder */}
        <Button
          label="Mitglieder"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callMitglieder}
        />

        {/* 3) Veranstaltungen */}
        <Button
          label="Veranstaltungen"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callVeranstaltungen}
        />

        {/* 4) Teilnehmer */}
        <Button
          label="Teilnehmer"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callTeilnehmer}
        />

        {/* 5) Kosten */}
        <Button
          label="Kosten"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callKosten}
        />

        {/* 6) Reisekosten */}
        <Button
          label="Reisekosten"
          className="w-full bg-blue-700 text-white font-bold p-6 text-xl rounded hover:bg-blue-600 whitespace-normal"
          onClick={callReisekosten}
        />

        {/* 7) Anmeldung */}
        <Button
          label="Anmeldung"
          className="w-full bg-red-700 text-white font-bold p-6 text-xl rounded hover:bg-red-600 whitespace-normal"
          onClick={callAnmeldung}
        />

        {/* 8) Abrechnung */}
        <Button
          label="Abrechnung"
          className="w-full bg-yellow-500 text-gray-900 font-bold p-6 text-xl rounded hover:bg-yellow-400 whitespace-normal"
          onClick={callAbrechnung}
        />

        {/* 9) Teilnehmerliste */}
        <Button
          label="Teilnehmerliste"
          className="w-full bg-yellow-500 text-gray-900 font-bold p-6 text-xl rounded hover:bg-yellow-400 whitespace-normal"
          onClick={callTeilnehmerliste}
        />

        {/* 10) Reisekosten Ausgabe */}
        <Button
          label="Reisekosten Ausgabe"
          className="w-full bg-yellow-500 text-gray-900 font-bold p-6 text-xl rounded hover:bg-yellow-400 whitespace-normal"
          onClick={callAusgabeReisekosten}
        />

        {/* 11) Erhebungsbogen */}
        <Button
          label="Erhebungsbogen"
          className="w-full bg-yellow-500 text-gray-900 font-bold p-6 text-xl rounded hover:bg-yellow-400 whitespace-normal"
          onClick={callErhebungsbogen}
        />
      </div>
    </div>
  );
};

export default StartMenue;