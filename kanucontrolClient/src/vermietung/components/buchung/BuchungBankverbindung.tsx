import { useEffect, useState } from "react";
import { Alert, Box, MenuItem, TextField, Typography } from "@mui/material";
import { getMietobjekt } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { BankverbindungAuswahl } from "@/vermietung/types/Bankverbindung";

interface Props {
  mietobjektId: number;
  value: BankverbindungAuswahl;
  disabled: boolean;
  onChange: (value: BankverbindungAuswahl) => void;
}

export function BuchungBankverbindung({ mietobjektId, value, disabled, onChange }: Props) {
  const [objekt, setObjekt] = useState<Mietobjekt | null>(null);
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    let live = true;
    setObjekt(null);
    setError(false);
    if (!mietobjektId) { setLoading(false); return; }
    setLoading(true);
    getMietobjekt(mietobjektId)
      .then((data) => { if (live) setObjekt(data); })
      .catch(() => { if (live) setError(true); })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [mietobjektId, disabled]);

  const current = objekt?.id === mietobjektId ? objekt : null;
  const bank = value === "ALTERNATIV" ? current?.bankverbindungAlternativ : current?.bankverbindungPrimaer;
  return <Box sx={{ display: "grid", gap: 1, minWidth: 0, overflowWrap: "anywhere" }}>
    <TextField select label="Bankverbindung für diese Buchung" value={value}
      disabled={disabled || loading || !current} fullWidth size="small"
      onChange={(event) => onChange(event.target.value as BankverbindungAuswahl)}>
      <MenuItem value="PRIMAER">Primär (Standard)</MenuItem>
      <MenuItem value="ALTERNATIV" disabled={!current?.bankverbindungAlternativ?.iban?.trim()}>Alternativ</MenuItem>
    </TextField>
    {loading && <Typography variant="body2" color="text.secondary">Bankverbindungen werden geladen …</Typography>}
    {error && <Alert severity="error">Die Bankverbindungen konnten nicht geladen werden.</Alert>}
    {!loading && current && !bank?.iban && <Alert severity="info">
      Für diese Auswahl ist beim Mietobjekt noch keine Bankverbindung hinterlegt.
    </Alert>}
    {bank?.iban && <Box>
      {bank.kontoinhaber && <Typography variant="body2">Kontoinhaber: {bank.kontoinhaber}</Typography>}
      {bank.bankName && <Typography variant="body2">Bank: {bank.bankName}</Typography>}
      <Typography variant="body2">IBAN: {bank.iban}</Typography>
      {bank.bic && <Typography variant="body2">BIC: {bank.bic}</Typography>}
    </Box>}
  </Box>;
}
