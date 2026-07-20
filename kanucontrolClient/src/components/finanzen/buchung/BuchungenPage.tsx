import { Button, Typography, Divider, Stack } from "@mui/material";

import { useEffect, useState, useCallback } from "react";

import SingleBelegRow from "@/components/finanzen/buchung/SingleBelegRow";
import MultiBelegAccordion from "@/components/finanzen/buchung/MultiBelegAccordion";
import BuchungDialog from "@/components/finanzen/buchung/BuchungDialog";
import BelegDialog from "@/components/finanzen/buchung/BelegDialog";
import BelegMitBuchungDialog from "@/components/finanzen/buchung/BelegMitBuchungDialog";
import FinanzSummary from "@/components/common/FinanzSummary";
import { AbrechnungsStatus } from "@/api/enums/AbrechnungsStatus";
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
import { istInBeleglisteSichtbar } from "@/api/utils/belegUtils";
import { fontSize, spacing } from "@/theme/ui";

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

  const handleEditBeleg = (beleg: AbrechnungBeleg) => {
    setEditingBeleg(beleg);
    setEditDialogOpen(true);
  };

  const handleAddPosition = (beleg: AbrechnungBeleg) => {
    setSelectedBeleg(beleg);
    setEditingBuchung(undefined);
    setDialogTyp("KOSTEN");
    setBuchungDialogOpen(true);
  };

  const handleEditPosition = (beleg: AbrechnungBeleg, buchung: Buchung) => {
    setSelectedBeleg(beleg);
    setEditingBuchung(buchung);
    setDialogTyp(kategorieZuTyp[buchung.kategorie]);
    setBuchungDialogOpen(true);
  };

  const handleDeletePosition = async (belegId: number, buchungId: number) => {
    if (!confirm("Position wirklich löschen?")) return;
    await deleteBuchung(veranstaltungId, belegId, buchungId);
    await load();
  };

  const handleDeleteBeleg = async (belegId: number) => {
    if (!confirm("Beleg komplett löschen?")) return;
    await deleteBeleg(veranstaltungId, belegId);
    await load();
  };

  /* =========================================================
     UI
     ========================================================= */

  return (
    <>
      <FinanzSummary
        kosten={abrechnung.finanz.kosten}
        einnahmen={abrechnung.finanz.einnahmen}
        eigenanteil={abrechnung.finanz.saldo}
        kjfpZuschuss={abrechnung.finanz.kjfpZuschuss}
      />
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "stretch", sm: "center" }}
        spacing={spacing.md}
        sx={{ mb: spacing.section }}
      >
        <Typography
          variant="h5"
          sx={{
            fontSize: fontSize.pageTitle,
          }}
        >
          Abrechnung – Belege
        </Typography>

        {abrechnung.status !== AbrechnungsStatus.ABGESCHLOSSEN && (
          <Button
            variant="contained"
            onClick={() => setCreateDialogOpen(true)}
            sx={{
              alignSelf: { xs: "stretch", sm: "flex-end" },
            }}
          >
            + Beleg anlegen
          </Button>
        )}
      </Stack>

      <Divider sx={{ mb: 3 }} />

      {[...abrechnung.belege]
        .sort((a, b) => new Date(b.datum).getTime() - new Date(a.datum).getTime())
        .map((beleg) => {
          const sichtbarePositionen = beleg.positionen.filter(istInBeleglisteSichtbar);
          if (sichtbarePositionen.length === 0) {
            return null;
          }

          const sichtbarerBeleg = {
            ...beleg,
            positionen: sichtbarePositionen,
          };

          const Component = sichtbarePositionen.length > 1 ? MultiBelegAccordion : SingleBelegRow;

          return (
            <Component
              key={beleg.id}
              beleg={sichtbarerBeleg}
              readOnly={abrechnung.status === AbrechnungsStatus.ABGESCHLOSSEN}
              onEditBeleg={handleEditBeleg}
              onAddPosition={handleAddPosition}
              onEditPosition={handleEditPosition}
              onDeletePosition={handleDeletePosition}
              onDeleteBeleg={handleDeleteBeleg}
            />
          );
        })}

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
    </>
  );
}
