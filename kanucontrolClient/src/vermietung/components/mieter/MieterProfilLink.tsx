import { Button } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export default function MieterProfilLink({ personId, buchungId }: { personId?: number; buchungId?: number }) {
  const ausgewaehlt = !!personId && personId > 0;
  return <Button component={RouterLink} size="small" variant="outlined"
    to={ausgewaehlt ? `/vermietung/mieter?personId=${personId}` : "/vermietung/mieter"}
    state={buchungId ? { returnTo: `/vermietung/buchung?buchungId=${buchungId}` } : undefined}>
    {ausgewaehlt ? "Zum Mieter" : "Zur Mieterliste / Mieter anlegen"}
  </Button>;
}
