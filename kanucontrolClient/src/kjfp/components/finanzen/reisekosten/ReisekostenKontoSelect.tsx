import { MenuItem, TextField } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getFinanzgruppen } from "@/kjfp/api/services/finanzgruppenApi";

export default function ReisekostenKontoSelect({ veranstaltungId, value, onChange, disabled = false }: {
  veranstaltungId: number; value: number | null; onChange: (id: number) => void; disabled?: boolean;
}) {
  const { data = [], isError } = useQuery({ queryKey: ["finanzgruppen", veranstaltungId],
    queryFn: () => getFinanzgruppen(veranstaltungId) });
  return <TextField select required fullWidth label="Konto für Belegnummer" value={value ?? ""}
    disabled={disabled} onChange={(event) => onChange(Number(event.target.value))}
    error={isError || value === null}
    helperText={isError ? "Konten konnten nicht geladen werden." : value === null ? "Bitte dem Fahrer ein Konto zuordnen." : "Konto der Fahrkostenabrechnung"}>
    {data.map((konto) => <MenuItem key={konto.id} value={konto.id}>{konto.kuerzel}</MenuItem>)}
  </TextField>;
}
