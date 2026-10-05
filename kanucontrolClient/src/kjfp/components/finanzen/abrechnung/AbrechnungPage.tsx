import { Typography } from "@mui/material";

interface Props {
  veranstaltungId: number;
}

export default function AbrechnungPage({ veranstaltungId }: Props) {
  return <Typography>Abrechnung für Veranstaltung {veranstaltungId}</Typography>;
}
