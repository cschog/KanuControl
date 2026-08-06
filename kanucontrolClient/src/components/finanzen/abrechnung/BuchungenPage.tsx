import {
  Alert,
  Button,
  Typography,
  Divider,
  Stack,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useEffect, useState, useCallback, useMemo } from "react";
import BelegDokumentDialog from "@/components/finanzen/abrechnung/BelegDokumentDialog";

import { berechneBelegsumme } from "@/api/utils/belegUtils";
import { formatGermanDate } from "@/utils/dateUtils";

import Money from "@/components/common/Money";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

import { FinanzKategorie } from "@/api/types/finanz";
import { upload } from "@/api/services/belegDokumentApi";
import FinanzgruppenBelegeAccordion from "@/components/finanzen/abrechnung/FinanzgruppenBelegeAccordion";
import BuchungDialog from "@/components/finanzen/abrechnung/BuchungDialog";
import BelegDialog from "@/components/finanzen/abrechnung/BelegDialog";
import BelegMitBuchungDialog from "@/components/finanzen/abrechnung/BelegMitBuchungDialog";
import FinanzSummary from "@/components/common/FinanzSummary";
import { AbrechnungsStatus } from "@/api/enums/AbrechnungsStatus";
import {
  getAbrechnung,
  addBuchung,
  updateBuchung,
  deleteBuchung,
  deleteBeleg as deleteBelegApi,
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
import FinanzpositionenAccordion from "@/components/simulation/FinanzpositionenAccordion";

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

  const [dokumentDialogOpen, setDokumentDialogOpen] = useState(false);
  const [selectedDokumentBeleg, setSelectedDokumentBeleg] = useState<AbrechnungBeleg | null>(null);
  const [deleteBeleg, setDeleteBeleg] = useState<AbrechnungBeleg | null>(null);
  const [deletePosition, setDeletePosition] = useState<{
    belegId: number;
    buchung: Buchung;
  } | null>(null);

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

  const finanzpositionen = useMemo(() => {
    if (!abrechnung) {
      return [];
    }

    const positionen = Object.values(
      abrechnung.belege
        .flatMap((beleg) => beleg.positionen.filter(istInBeleglisteSichtbar))
        .reduce<
          Record<
            FinanzKategorie,
            {
              kategorie: FinanzKategorie;
              betrag: number;
            }
          >
        >(
          (map, position) => {
            const vorhanden = map[position.kategorie];

            if (vorhanden) {
              vorhanden.betrag += position.betrag;
            } else {
              map[position.kategorie] = {
                kategorie: position.kategorie,
                betrag: position.betrag,
              };
            }

            return map;
          },
          {} as Record<FinanzKategorie, { kategorie: FinanzKategorie; betrag: number }>,
        ),
    );

    // KJFP-Zuschuss ergänzen
    if (abrechnung.finanz.kjfpZuschuss !== 0) {
      positionen.push({
        kategorie: "KJFP_ZUSCHUSS" as FinanzKategorie,
        betrag: abrechnung.finanz.kjfpZuschuss,
      });
    }

    return positionen;
  }, [abrechnung]);

  if (!abrechnung) {
    return <CircularProgress />;
  }

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

  const handleCreateBeleg = async (
    data: {
      beleg: BelegCreate;
      buchung: BuchungCreate;
    },
    files: File[],
  ) => {
    const beleg = await createBelegWithBuchung(veranstaltungId, data);

    if (files.length > 0) {
      await Promise.all(files.map((file) => upload(beleg.id, file)));
    }

    setCreateDialogOpen(false);

    await load();
  };

  const handleDeletePosition = (belegId: number, buchung: Buchung) => {
    setDeletePosition({ belegId, buchung });
  };

  const confirmDeletePosition = async () => {
    if (!deletePosition) {
      return;
    }

    await deleteBuchung(veranstaltungId, deletePosition.belegId, deletePosition.buchung.id);

    setDeletePosition(null);

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

  const handleShowDokumente = (beleg: AbrechnungBeleg) => {
    setSelectedDokumentBeleg(beleg);
    setDokumentDialogOpen(true);
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

  const confirmDeleteBeleg = async () => {
    if (!deleteBeleg) {
      return;
    }
    await deleteBelegApi(veranstaltungId, deleteBeleg.id);
    setDeleteBeleg(null);

    await load();
  };

  const handleDeleteBeleg = (beleg: AbrechnungBeleg) => {
    setDeleteBeleg(beleg);
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

      <Accordion defaultExpanded sx={{ mb: spacing.section }}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography
            sx={{
              fontWeight: 700,
              fontSize: fontSize.sectionTitle,
            }}
          >
            Belege ({abrechnung.belege.length})
          </Typography>
        </AccordionSummary>

        <AccordionDetails>
          <FinanzgruppenBelegeAccordion
            belege={abrechnung.belege}
            readOnly={abrechnung.status === AbrechnungsStatus.ABGESCHLOSSEN}
            onEditBeleg={handleEditBeleg}
            onShowDokumente={handleShowDokumente}
            onAddPosition={handleAddPosition}
            onEditPosition={handleEditPosition}
            onDeletePosition={handleDeletePosition}
            onDeleteBeleg={handleDeleteBeleg}
          />
        </AccordionDetails>
      </Accordion>

      <FinanzpositionenAccordion
        title="Finanzübersicht der Abrechnung"
        positionen={finanzpositionen}
      />

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

      {selectedDokumentBeleg && (
        <BelegDokumentDialog
          open={dokumentDialogOpen}
          belegId={selectedDokumentBeleg.id}
          onClose={() => {
            setDokumentDialogOpen(false);
            setSelectedDokumentBeleg(null);
          }}
        />
      )}

      <DeleteConfirmDialog
        open={deleteBeleg !== null}
        title="Beleg löschen"
        onClose={() => setDeleteBeleg(null)}
        onConfirm={() => void confirmDeleteBeleg()}
      >
        {deleteBeleg && (
          <Stack spacing={2}>
            <Typography color="text.secondary">
              Der folgende Beleg wird vollständig gelöscht.
            </Typography>

            <Divider />

            <Stack spacing={1}>
              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Aussteller</Typography>
                <Typography>{deleteBeleg.aussteller || "-"}</Typography>
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Datum</Typography>
                <Typography>{formatGermanDate(deleteBeleg.datum)}</Typography>
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Belegnummer</Typography>
                <Typography>{deleteBeleg.belegnummer}</Typography>
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Summe</Typography>

                <Money value={berechneBelegsumme(deleteBeleg)} />
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Dokumente</Typography>
                <Typography>{deleteBeleg.dokumente.length}</Typography>
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Positionen</Typography>
                <Typography>{deleteBeleg.positionen.length}</Typography>
              </Stack>
            </Stack>

            <Alert severity="warning">
              Alle Dokumente und Buchungen dieses Belegs werden ebenfalls gelöscht.
            </Alert>
          </Stack>
        )}
      </DeleteConfirmDialog>

      <DeleteConfirmDialog
        open={deletePosition !== null}
        title="Position löschen"
        onClose={() => setDeletePosition(null)}
        onConfirm={() => void confirmDeletePosition()}
      >
        {deletePosition && (
          <Stack spacing={2}>
            <Typography color="text.secondary">
              Die folgende Buchungsposition wird gelöscht.
            </Typography>

            <Divider />

            <Stack spacing={1}>
              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Beschreibung</Typography>
                <Typography>{deletePosition.buchung.beschreibung}</Typography>
              </Stack>

              <Stack direction="row" justifyContent="space-between">
                <Typography color="text.secondary">Betrag</Typography>
                <Money value={deletePosition.buchung.betrag} />
              </Stack>
            </Stack>

            <Alert severity="warning">Diese Buchungsposition wird dauerhaft gelöscht.</Alert>
          </Stack>
        )}
      </DeleteConfirmDialog>
    </>
  );
}
