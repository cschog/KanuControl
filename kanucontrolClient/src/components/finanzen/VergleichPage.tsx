import { Typography } from "@mui/material";

interface Props {
  veranstaltungId: number;
}

export default function VergleichPage({ veranstaltungId }: Props) {
  return <Typography>Vergleich für Veranstaltung {veranstaltungId}</Typography>;
}
