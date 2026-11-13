import { formatPersonName } from "@/core/utils/personName";
import { Link } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import type { Mieter } from "@/vermietung/types/Mieter";

export default function MieterBuchungenLink({ mieter }: { mieter: Mieter }) {
  return <Link component={RouterLink} to={`/vermietung/buchung?mieterId=${mieter.personId}`}
    onClick={event => event.stopPropagation()} underline="hover"
    aria-label={`Buchungen von ${formatPersonName(mieter.name, mieter.vorname, "first")} anzeigen`}>
    {mieter.buchungen} {mieter.buchungen === 1 ? "Buchung" : "Buchungen"}
  </Link>;
}
