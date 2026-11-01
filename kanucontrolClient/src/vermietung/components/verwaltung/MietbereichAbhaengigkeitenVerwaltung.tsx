import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  ListItemText,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getMietbereiche, setMietbereichVoraussetzungen } from "@/vermietung/api/mietbereichApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

const sortIds = (ids: number[]) => [...ids].sort((a, b) => a - b);
const gleicheIds = (a: number[], b: number[]) =>
  JSON.stringify(sortIds(a)) === JSON.stringify(sortIds(b));

export default function MietbereichAbhaengigkeitenVerwaltung() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [mietobjektId, setMietobjektId] = useState<number | "">("");
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [urspruenglicheMietbereiche, setUrspruenglicheMietbereiche] = useState<Mietbereich[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    getMietobjekte()
      .then((data) => {
        if (!active) return;
        setMietobjekte(data);
        setMietobjektId(data[0]?.id ?? "");
      })
      .catch((err) => {
        console.error("Mietobjekte konnten nicht geladen werden:", err);
        if (active) setError("Die Mietobjekte konnten nicht geladen werden.");
      })
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (mietobjektId === "") {
      setMietbereiche([]);
      setUrspruenglicheMietbereiche([]);
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);
    getMietbereiche(mietobjektId)
      .then((data) => {
        if (!active) return;
        setMietbereiche(data);
        setUrspruenglicheMietbereiche(data);
      })
      .catch((err) => {
        console.error("Mietbereiche konnten nicht geladen werden:", err);
        if (active) setError("Die Mietbereiche konnten nicht geladen werden.");
      })
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [mietobjektId]);

  async function speichern(bereich: Mietbereich) {
    if (mietobjektId === "") return;
    try {
      setSavingId(bereich.id);
      setError(null);
      setSuccess(null);
      const aktualisiert = await setMietbereichVoraussetzungen(
        mietobjektId,
        bereich.id,
        bereich.erforderlicheMietbereichIds ?? [],
      );
      setMietbereiche((aktuell) => aktuell.map((item) => item.id === bereich.id ? aktualisiert : item));
      setUrspruenglicheMietbereiche((aktuell) => aktuell.map((item) => item.id === bereich.id ? aktualisiert : item));
      setSuccess(`Abhängigkeiten für „${bereich.bezeichnung}“ gespeichert.`);
    } catch (err) {
      console.error("Abhängigkeiten konnten nicht gespeichert werden:", err);
      setError("Die Abhängigkeiten konnten nicht gespeichert werden. Prüfe, ob die Auswahl einen Abhängigkeitskreis erzeugt.");
    } finally {
      setSavingId(null);
    }
  }

  function aendereVoraussetzungen(bereichId: number, ids: number[]) {
    setSuccess(null);
    setMietbereiche((aktuell) => aktuell.map((bereich) =>
      bereich.id === bereichId ? { ...bereich, erforderlicheMietbereichIds: ids } : bereich,
    ));
  }

  if (loading && mietobjekte.length === 0) {
    return <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}><CircularProgress /></Box>;
  }

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
      <Typography variant="body2" color="text.secondary">
        Lege fest, welche Mietbereiche Voraussetzung für einen anderen Bereich sind. Zum Beispiel kann ein Grill nur zusammen mit dem EG gebucht werden.
      </Typography>

      {error && <Alert severity="error">{error}</Alert>}
      {success && <Alert severity="success">{success}</Alert>}

      <TextField
        select
        label="Mietobjekt"
        value={mietobjektId}
        onChange={(event) => setMietobjektId(Number(event.target.value))}
        sx={{ maxWidth: 480 }}
      >
        {mietobjekte.map((objekt) => <MenuItem key={objekt.id} value={objekt.id}>{objekt.bezeichnung}</MenuItem>)}
      </TextField>

      {mietobjektId !== "" && !loading && mietbereiche.length === 0 && (
        <Alert severity="info">Für dieses Mietobjekt sind noch keine Mietbereiche angelegt.</Alert>
      )}

      {!loading && mietbereiche.map((bereich) => {
        const voraussetzungen = bereich.erforderlicheMietbereichIds ?? [];
        const original = (urspruenglicheMietbereiche.find((item) => item.id === bereich.id)?.erforderlicheMietbereichIds ?? []);
        const geaendert = !gleicheIds(voraussetzungen, original);
        const auswahl = mietbereiche.filter((item) => item.mietbar && item.id !== bereich.id);

        return (
          <Paper key={bereich.id} variant="outlined" sx={{ p: 2, display: "grid", gridTemplateColumns: { xs: "1fr", md: "minmax(180px, 0.7fr) minmax(260px, 1.3fr) auto" }, gap: 2, alignItems: "center" }}>
            <Box>
              <Typography fontWeight={600}>{bereich.bezeichnung}</Typography>
              <Typography variant="caption" color="text.secondary">{bereich.mietbar ? "Mietbar" : "Nicht mietbar"}</Typography>
            </Box>
            <TextField
              select
              label="Erfordert diese Mietbereiche"
              value={voraussetzungen}
              disabled={!bereich.mietbar || savingId === bereich.id}
              slotProps={{
                select: {
                  multiple: true,
                  renderValue: (selected) => {
                    const ids = selected as number[];
                    return mietbereiche.filter((item) => ids.includes(item.id)).map((item) => item.bezeichnung).join(", ") || "Keine Voraussetzung";
                  },
                },
              }}
              onChange={(event) => {
                const value = event.target.value;
                aendereVoraussetzungen(bereich.id, typeof value === "string" ? value.split(",").map(Number) : value as number[]);
              }}
              helperText="Auswahl ist nur möglich, wenn alle Voraussetzungen mitgebucht werden."
            >
              {auswahl.map((item) => (
                <MenuItem key={item.id} value={item.id}>
                  <Checkbox checked={voraussetzungen.includes(item.id)} />
                  <ListItemText primary={item.bezeichnung} />
                </MenuItem>
              ))}
            </TextField>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              disabled={!geaendert || savingId === bereich.id || !bereich.mietbar}
              onClick={() => void speichern(bereich)}
            >
              {savingId === bereich.id ? "Speichert …" : "Speichern"}
            </Button>
          </Paper>
        );
      })}
    </Box>
  );
}
