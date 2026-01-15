import { Button } from "primereact/button";
import "primereact/resources/themes/md-light-indigo/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import { useTenant } from "@/auth/useTenant";

const StartMenue = () => {
  const tenant = useTenant();

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">KanuControl</h1>

      <h4 className="text-lg mb-2">Mandant:</h4>
      <p className="mb-6">{tenant?.displayName ?? "Kein Mandant zugeordnet"}</p>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <Button
          label="Vereine"
          onClick={() => (window.location.href = "/vereine")}
        />
        <Button
          label="Mitglieder"
          onClick={() => (window.location.href = "/personen")}
        />
        <Button
          label="Veranstaltungen"
          onClick={() => (window.location.href = "/veranstaltungen")}
        />
        <Button
          label="Teilnehmer"
          onClick={() => (window.location.href = "/teilnehmer")}
        />
        <Button
          label="Kosten"
          onClick={() => (window.location.href = "/kosten")}
        />
        <Button
          label="Reisekosten"
          onClick={() => (window.location.href = "/reisekosten")}
        />
        <Button
          label="Anmeldung"
          onClick={() => (window.location.href = "/anmeldung")}
        />
        <Button
          label="Abrechnung"
          onClick={() => (window.location.href = "/abrechnung")}
        />
        <Button
          label="Teilnehmerliste"
          onClick={() => (window.location.href = "/teilnehmerliste")}
        />
        <Button
          label="Reisekosten Ausgabe"
          onClick={() => (window.location.href = "/ausgabeReisekosten")}
        />
        <Button
          label="Erhebungsbogen"
          onClick={() => (window.location.href = "/erhebungsbogen")}
        />
      </div>
    </div>
  );
};

export default StartMenue;
