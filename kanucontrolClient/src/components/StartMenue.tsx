import { useCallback } from "react";
import { Button } from "primereact/button";
import "primereact/resources/themes/md-light-indigo/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";

const StartMenue = () => {
  const callVereine = useCallback(() => (window.location.href = "/vereine"), []);
  const callMitglieder = useCallback(() => (window.location.href = "/personen"), []);
  const callVeranstaltungen = useCallback(() => (window.location.href = "/veranstaltungen"), []);
  const callTeilnehmer = useCallback(() => (window.location.href = "/teilnehmer"), []);
  const callKosten = useCallback(() => (window.location.href = "/kosten"), []);
  const callReisekosten = useCallback(() => (window.location.href = "/reisekosten"), []);
  const callTeilnehmerliste = useCallback(() => (window.location.href = "/teilnehmerliste"), []);
  const callErhebungsbogen = useCallback(() => (window.location.href = "/erhebungsbogen"), []);
  const callAnmeldung = useCallback(() => (window.location.href = "/anmeldung"), []);
  const callAbrechnung = useCallback(() => (window.location.href = "/abrechnung"), []);
  const callAusgabeReisekosten = useCallback(() => (window.location.href = "/ausgabeReisekosten"), []);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">KanuControl</h1>
      <h4 className="text-lg mb-2">Aktive Veranstaltung</h4>
      <p className="mb-6">Test-Veranstaltung</p>

      {/* Main container for two blocks */}
      <div className="space-y-8">
        {/* First block: Main menu */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <Button
            label="Vereine"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callVereine}
          />
		   <Button
            label="Veranstaltungen"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callVeranstaltungen}
          />
         <Button
            label="Kosten"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callKosten}
          />
		   <Button
            label="Mitglieder"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callMitglieder}
          />
         
          <Button
            label="Teilnehmer"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callTeilnehmer}
          />
		  
          
          <Button
            label="Reisekosten"
            className="w-full bg-blue-700 text-white font-bold p-4 rounded hover:bg-blue-600 whitespace-normal"
            onClick={callReisekosten}
          />
        </div>

        {/* Second block: Abrechnung menu */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <Button
            label="Teilnehmerliste"
            className="w-full bg-yellow-500 text-gray-900 font-bold p-4 rounded hover:bg-yellow-400 whitespace-normal"
            onClick={callTeilnehmerliste}
          />
		  <Button
            label="Anmeldung"
            className="w-full bg-red-700 text-white font-bold p-4 rounded hover:bg-red-600 whitespace-normal"
            onClick={callAnmeldung}
          />
		   <Button
            label="Abrechnung"
            className="w-full bg-yellow-500 text-gray-900 font-bold p-4 rounded hover:bg-yellow-400 whitespace-normal"
            onClick={callAbrechnung}
          />
          <Button
            label="Erhebungsbogen"
            className="w-full bg-yellow-500 text-gray-900 font-bold p-4 rounded hover:bg-yellow-400 whitespace-normal"
            onClick={callErhebungsbogen}
          />
          
         
          <Button
            label="Reisekosten Ausgabe"
            className="w-full bg-yellow-500 text-gray-900 font-bold p-4 rounded hover:bg-yellow-400 whitespace-normal"
            onClick={callAusgabeReisekosten}
          />
        </div>
      </div>
    </div>
  );
};

export default StartMenue;