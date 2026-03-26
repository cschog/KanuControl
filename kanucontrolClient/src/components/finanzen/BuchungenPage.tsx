import {
  Box,
  Button,
  Typography,
  Stack,
  Divider,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
} from "@mui/material";

import { useEffect, useState, useCallback } from "react";
import { getAbrechnung, addBuchung, updateBuchung,deleteBuchung, deleteBeleg } from "@/api/client/abrechnungApi";
import { getFinanzgruppen, FinanzGruppe } from "@/api/client/finanzgruppenApi";
import BuchungDialog from "@/components/finanzen/BuchungDialog";
import { createBelegWithBuchung } from "@/api/client/abrechnungApi";
import BelegMitBuchungDialog from "@/components/finanzen/BelegMitBuchungDialog";

import { AbrechnungDetail, AbrechnungBeleg, Buchung, BuchungCreate, BelegCreate } from "@/api/types/abrechnung";

import { kategorieZuTyp } from "@/api/types/finanz";

interface Props {
  veranstaltungId: number;
}

export default function BuchungenPage({ veranstaltungId }: Props) {
  const [abrechnung, setAbrechnung] = useState<AbrechnungDetail | null>(null);

  const [finanzgruppen, setFinanzgruppen] = useState<FinanzGruppe[]>([]);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Buchung | undefined>();
  const [selectedBeleg, setSelectedBeleg] = useState<AbrechnungBeleg | null>(null);

  const [belegDialogOpen, setBelegDialogOpen] = useState(false);

  const [dialogTyp, setDialogTyp] = useState<"KOSTEN" | "EINNAHME">("KOSTEN");

  /* ================= LOAD ================= */

  const load = useCallback(async () => {
    const abrechnungData = await getAbrechnung(veranstaltungId);
    setAbrechnung(abrechnungData);

    const gruppenData = await getFinanzgruppen(veranstaltungId);
    setFinanzgruppen(gruppenData);
  }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load]);

  if (!abrechnung) return null;

  const { kosten, einnahmen, saldo } = abrechnung.finanz;

  /* ================= CRUD ================= */

  const handleSave = async (data: BuchungCreate) => {
    if (!selectedBeleg) return;

    if (editing) {
      await updateBuchung(veranstaltungId, selectedBeleg.id, editing.id, data);
    } else {
      await addBuchung(veranstaltungId, selectedBeleg.id, data);
    }

    setDialogOpen(false);
    setEditing(undefined);
    setSelectedBeleg(null);
    await load();
  };

  const handleCreate = async (data: { beleg: BelegCreate; buchung: BuchungCreate }) => {
    await createBelegWithBuchung(veranstaltungId, data);
    setBelegDialogOpen(false);
    await load();
  };

  /* ================= RENDER ================= */

  const renderBeleg = (beleg: AbrechnungBeleg) => (
    <Box key={beleg.id} mb={4}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
        <Box>
          <Typography variant="h6">
            {beleg.belegnummer} ({beleg.kuerzel})
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {beleg.datum} – {beleg.beschreibung}
          </Typography>
        </Box>

        {abrechnung.status !== "ABGESCHLOSSEN" && (
          <Stack direction="row" spacing={1}>
            <Button
              size="small"
              variant="contained"
              onClick={() => {
                setSelectedBeleg(beleg);
                setEditing(undefined);
                setDialogTyp("KOSTEN");
                setDialogOpen(true);
              }}
            >
              + Position
            </Button>

            <Button
              size="small"
              color="error"
              onClick={async () => {
                if (!confirm("Beleg komplett löschen?")) return;

                await deleteBeleg(veranstaltungId, beleg.id);
                await load();
              }}
            >
              Löschen
            </Button>
          </Stack>
        )}
      </Stack>

      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Datum</TableCell>
              <TableCell>Kategorie</TableCell>
              <TableCell>Beschreibung</TableCell>
              <TableCell align="right">Betrag (€)</TableCell>
              {abrechnung.status !== "ABGESCHLOSSEN" && <TableCell />}
            </TableRow>
          </TableHead>

          <TableBody>
            {beleg.positionen.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{p.datum}</TableCell>
                <TableCell>{p.kategorie.replaceAll("_", " ")}</TableCell>

                <TableCell>{p.beschreibung}</TableCell>
                <TableCell align="right">{p.betrag.toFixed(2)}</TableCell>

                {abrechnung.status !== "ABGESCHLOSSEN" && (
                  <TableCell>
                    <Button
                      size="small"
                      onClick={() => {
                        setSelectedBeleg(beleg);
                        setEditing(p);
                        setDialogTyp(kategorieZuTyp[p.kategorie]);
                        setDialogOpen(true);
                      }}
                    >
                      Bearbeiten
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      onClick={async () => {
                        if (!confirm("Position wirklich löschen?")) return;

                        await deleteBuchung(veranstaltungId, beleg.id, p.id);
                        await load();
                      }}
                    >
                      Löschen
                    </Button>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );

  /* ================= UI ================= */

  return (
    <Box p={3}>
      <Typography variant="h5" gutterBottom>
        Abrechnung – Belege
      </Typography>

      {abrechnung.status !== "ABGESCHLOSSEN" && (
        <Button variant="contained" sx={{ mb: 2 }} onClick={() => setBelegDialogOpen(true)}>
          + Beleg anlegen
        </Button>
      )}

      <Stack direction="row" spacing={4} mb={3}>
        <Typography>💸 Kosten: {kosten.toFixed(2)} €</Typography>
        <Typography>💰 Einnahmen: {einnahmen.toFixed(2)} €</Typography>
        <Typography>📊 Saldo: {saldo.toFixed(2)} €</Typography>
      </Stack>

      <Divider sx={{ mb: 3 }} />

      {abrechnung.belege.map(renderBeleg)}

      <BuchungDialog
        open={dialogOpen}
        typ={dialogTyp}
        initialData={editing}
        onClose={() => {
          setDialogOpen(false);
          setEditing(undefined);
          setSelectedBeleg(null);
        }}
        onSave={handleSave}
      />

      <BelegMitBuchungDialog
        open={belegDialogOpen}
        kuerzelListe={finanzgruppen.map((g) => g.kuerzel)}
        onClose={() => setBelegDialogOpen(false)}
        onSave={handleCreate}
      />
    </Box>
  );
}
