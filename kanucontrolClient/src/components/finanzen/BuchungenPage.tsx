import { Typography } from "@mui/material";

interface Props {
  veranstaltungId: number;
}

export default function BuchungenPage({ veranstaltungId }: Props) {
  return <Typography>Buchungen für Veranstaltung {veranstaltungId}</Typography>;
}
