import { Button } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export default function MieterStammdatenLink({ personId, buchungId, fehler }: {
  personId: number; buchungId: number; fehler: string;
}) {
  if (!personId || !/mieter|stammdaten|erstattungskonto/i.test(fehler)) return null;
  const feld = /vorname/i.test(fehler) ? "vorname"
    : /nachname/i.test(fehler) ? "name"
    : /e-mail|email/i.test(fehler) ? "email"
    : /iban/i.test(fehler) ? "iban" : "";
  const params = new URLSearchParams({ personId: String(personId), bearbeiten: "1" });
  if (feld) params.set("feld", feld);
  return <Button color="inherit" size="small" component={RouterLink}
    to={`/vermietung/mieter?${params.toString()}`}
    state={{ returnTo: `/vermietung/buchung?buchungId=${buchungId}` }}>
    Mieter bearbeiten
  </Button>;
}
