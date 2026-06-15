import { Box, Button, Typography, Stack, Divider } from "@mui/material";

import { useEffect, useState, useCallback } from "react";

import BelegCard from "@/components/finanzen/buchung/BelegCard";
import BuchungDialog from "@/components/finanzen/buchung/BuchungDialog";
import BelegDialog from "@/components/finanzen/buchung/BelegDialog";
import BelegMitBuchungDialog from "@/components/finanzen/buchung/BelegMitBuchungDialog";

import {
  getAbrechnung,
  addBuchung,
  updateBuchung,
  deleteBuchung,
  deleteBeleg,
  updateBeleg,
  createBelegWithBuchung,
} from "@/api/services/abrechnungApi";

import { getFinanzgruppen, FinanzGruppe } from "@/api/services/finanzgruppenApi";

import {
  AbrechnungDetail,
  AbrechnungBeleg,
  Buchung,
  BuchungCreate,
  BelegCreate,
} from "@/api/types/abrechnung";

import { kategorieZuTyp } from "@/api/types/finanz";

interface Props {
  veranstaltungId: number;
}

export default function BuchungenPage({ veranstaltungId }: Props) {
  const [abrechnung, setAbrechnung] = useState<AbrechnungDetail | null>(null);

  const [finanzgruppen, setFinanzgruppen] = useState<FinanzGruppe[]>([]);

  /* =========================================================
     BUCHUNG DIALOG
     ========================================================= */

  const [buchungDialogOpen, setBuchungDialogOpen] = useState(false);
  const [editingBuchung, setEditingBuchung] = useState<Buchung | undefined>();
  const [selectedBeleg, setSelectedBeleg] = useState<AbrechnungBeleg | null>(null);
  const [dialogTyp, setDialogTyp] = useState<"KOSTEN" | "EINNAHME">("KOSTEN");

  /* =========================================================
     BELEG DIALOGE
     ========================================================= */

  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  const [editDialogOpen, setEditDialogOpen] = useState(false);

  const [editingBeleg, setEditingBeleg] = useState<AbrechnungBeleg | null>(null);

  /* =========================================================
     LOAD
     ========================================================= */

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

  /* =========================================================
     BUCHUNG CRUD
     ========================================================= */

  const handleSaveBuchung = async (data: BuchungCreate) => {
    if (!selectedBeleg) return;

    if (editingBuchung) {
      await updateBuchung(veranstaltungId, selectedBeleg.id, editingBuchung.id, data);
    } else {
      await addBuchung(veranstaltungId, selectedBeleg.id, data);
    }

    setBuchungDialogOpen(false);

    setEditingBuchung(undefined);

    setSelectedBeleg(null);

    await load();
  };

  /* =========================================================
     CREATE BELEG
     ========================================================= */

  const handleCreateBeleg = async (data: { beleg: BelegCreate; buchung: BuchungCreate }) => {
    await createBelegWithBuchung(veranstaltungId, data);

    setCreateDialogOpen(false);

    await load();
  };

  /* =========================================================
     UPDATE BELEG
     ========================================================= */

  const handleUpdateBeleg = async (data: BelegCreate) => {
    if (!editingBeleg) return;

    await updateBeleg(veranstaltungId, editingBeleg.id, data);

    setEditDialogOpen(false);

    setEditingBeleg(null);

    await load();
  };

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box p={3}>
      <Typography variant="h5" gutterBottom>
        Abrechnung – Belege
      </Typography>

      {abrechnung.status !== "ABGESCHLOSSEN" && (
        <Button variant="contained" sx={{ mb: 2 }} onClick={() => setCreateDialogOpen(true)}>
          + Beleg anlegen
        </Button>
      )}

      <Stack
        direction={{
          xs: "column",
          sm: "row",
        }}
        spacing={{
          xs: 1,
          sm: 4,
        }}
        mb={3}
      >
        <Typography
          sx={{
            whiteSpace: "nowrap",
            fontSize: {
              xs: "0.95rem",
              md: "1.5rem",
            },
            fontWeight: 500,
          }}
        >
          💸 Kosten: {kosten.toFixed(2)} €
        </Typography>

        <Typography
          sx={{
            whiteSpace: "nowrap",
            fontSize: {
              xs: "0.95rem",
              md: "1.5rem",
            },
            fontWeight: 500,
          }}
        >
          💰 Einnahmen: {einnahmen.toFixed(2)} €
        </Typography>

        <Typography
          sx={{
            whiteSpace: "nowrap",
            fontSize: {
              xs: "0.95rem",
              md: "1.5rem",
            },
            fontWeight: 500,
          }}
        >
          📊 Saldo: {saldo.toFixed(2)} €
        </Typography>
      </Stack>

      <Divider sx={{ mb: 3 }} />

      {[...abrechnung.belege]
        .sort((a, b) => new Date(b.datum).getTime() - new Date(a.datum).getTime())
        .map((beleg) => (
          <BelegCard
            key={beleg.id}
            beleg={beleg}
            readOnly={abrechnung.status === "ABGESCHLOSSEN"}
            onEditBeleg={(beleg) => {
              setEditingBeleg(beleg);

              setEditDialogOpen(true);
            }}
            onAddPosition={(beleg) => {
              setSelectedBeleg(beleg);

              setEditingBuchung(undefined);

              setDialogTyp("KOSTEN");

              setBuchungDialogOpen(true);
            }}
            onEditPosition={(beleg, buchung) => {
              setSelectedBeleg(beleg);

              setEditingBuchung(buchung);

              setDialogTyp(kategorieZuTyp[buchung.kategorie]);

              setBuchungDialogOpen(true);
            }}
            onDeletePosition={async (belegId, buchungId) => {
              if (!confirm("Position wirklich löschen?")) return;

              await deleteBuchung(veranstaltungId, belegId, buchungId);

              await load();
            }}
            onDeleteBeleg={async (belegId) => {
              if (!confirm("Beleg komplett löschen?")) return;

              await deleteBeleg(veranstaltungId, belegId);

              await load();
            }}
          />
        ))}

      {/* =====================================================
          BUCHUNG DIALOG
         ===================================================== */}

      <BuchungDialog
        open={buchungDialogOpen}
        typ={dialogTyp}
        initialData={editingBuchung}
        onClose={() => {
          setBuchungDialogOpen(false);

          setEditingBuchung(undefined);

          setSelectedBeleg(null);
        }}
        onSave={handleSaveBuchung}
      />

      {/* =====================================================
          CREATE BELEG
         ===================================================== */}

      <BelegMitBuchungDialog
        open={createDialogOpen}
        kuerzelListe={finanzgruppen.map((g) => g.kuerzel)}
        onClose={() => setCreateDialogOpen(false)}
        onSave={handleCreateBeleg}
      />

      {/* =====================================================
          EDIT BELEG
         ===================================================== */}

      <BelegDialog
        open={editDialogOpen}
        kuerzelListe={finanzgruppen.map((g) => g.kuerzel)}
        initialData={editingBeleg ?? undefined}
        onClose={() => {
          setEditDialogOpen(false);

          setEditingBeleg(null);
        }}
        onSave={handleUpdateBeleg}
      />
    </Box>
  );
}
