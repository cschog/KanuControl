import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";

import { zahlungsnachweiseColumns } from "@/components/finanzen/beitraege/zahlungsnachweiseColumns";
import ZahlungsnachweisDialog from "@/components/finanzen/beitraege/ZahlungsnachweisDialog";
import { fontSize, padding, chip, layout, spacing } from "@/theme/ui";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { beitraegeColumns } from "@/components/finanzen/beitraege/beitraegeColumns";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

import {
  TeilnehmerBeitraegeResponseDTO,
  TeilnehmerListDTO,
  TeilnehmerBeitragSummaryDTO,
  ZahlungsnachweisDetailDTO,
  ZahlungsnachweisListDTO,
} from "@/api/types/beitraege";

import { useCallback, useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";

interface Props {
  veranstaltungId: number;
}

const BeitraegePage = ({ veranstaltungId }: Props) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [summary, setSummary] = useState<TeilnehmerBeitragSummaryDTO | null>(null);
  const [zahlungsnachweise, setZahlungsnachweise] = useState<ZahlungsnachweisListDTO[]>([]);
  const [data, setData] = useState<TeilnehmerListDTO[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [bearbeiteterZahlungsnachweis, setBearbeiteterZahlungsnachweis] =
    useState<ZahlungsnachweisDetailDTO | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteZahlungsnachweisId, setDeleteZahlungsnachweisId] = useState<number | null>(null);

  /* =========================================================
     ZAHLUNGSSTATUS
  ========================================================= */

  const zahlungsstatusLabel = {
    ROT: "Offen",
    GELB: "Teilbezahlt",
    GRUEN: "Bezahlt",
  } as const;

  const zahlungsstatusColor = {
    ROT: "error",
    GELB: "warning",
    GRUEN: "success",
  } as const;

  /* =========================================================
     ZAHLUNGSNACHWEISE
  ========================================================= */

  const handleEditZahlungsnachweis = async (id: number) => {
    try {
      const response = await apiClient.get<ZahlungsnachweisDetailDTO>(
        `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${id}`,
      );

      setBearbeiteterZahlungsnachweis(response.data);
      setDialogOpen(true);
    } catch (err) {
      console.error(err);
      setError("Zahlungsnachweis konnte nicht geladen werden.");
    }
  };

  const handleDeleteZahlungsnachweis = (id: number) => {
    setDeleteZahlungsnachweisId(id);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDeleteZahlungsnachweis = async () => {
    if (deleteZahlungsnachweisId === null) {
      return;
    }

    try {
      await apiClient.delete(
        `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${deleteZahlungsnachweisId}`,
      );

      setDeleteDialogOpen(false);
      setDeleteZahlungsnachweisId(null);

      await load();
    } catch (err) {
      console.error(err);
      setError("Zahlungsnachweis konnte nicht gelöscht werden.");
    }
  };

  const zahlungsnachweiseCols = zahlungsnachweiseColumns({
    onEdit: handleEditZahlungsnachweis,
    onDelete: handleDeleteZahlungsnachweis,
  });

  /* =========================================================
     LOAD
  ========================================================= */

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await apiClient.get<TeilnehmerBeitraegeResponseDTO>(
        `/veranstaltungen/${veranstaltungId}/beitraege`,
      );

      setSummary(response.data.summary);
      setZahlungsnachweise(response.data.zahlungsnachweise);
      setData(response.data.teilnehmer);
    } catch (err) {
      console.error(err);
      setError("Beiträge konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load]);

  /* =========================================================
     SUMMEN
  ========================================================= */

  const getBeitrag = (t: TeilnehmerListDTO) => t.sollBeitrag ?? 0;

  const summe = data.reduce((sum, t) => sum + getBeitrag(t), 0);

  const chipStyle = {
    fontSize: fontSize.pageTitle,
    fontWeight: "bold",

    height: chip.height,

    borderRadius: chip.borderRadius,

    "& .MuiChip-label": {
      px: chip.labelPadding,
    },
  };

  /* =========================================================
     RENDER
  ========================================================= */

  if (loading) {
    return (
      <Box sx={{ p: spacing.lg }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  const columns = beitraegeColumns();

  return (
    <Stack spacing={spacing.sm}>
      {/* =====================================================
          HEADER
      ===================================================== */}

      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: layout.kpiGrid,
          gap: spacing.chip,
        }}
      >
        <Chip
          label={`Teilnehmer: ${summary?.anzahlTeilnehmer ?? data.length}`}
          color="primary"
          sx={chipStyle}
        />

        <Chip
          label={`Soll: ${(summary?.sollSumme ?? summe).toFixed(2)} €`}
          color="info"
          sx={chipStyle}
        />

        <Chip
          label={`Bezahlt: ${(summary?.bezahltSumme ?? 0).toFixed(2)} €`}
          color="success"
          sx={chipStyle}
        />

        <Chip
          label={`Offen: ${(summary?.offenSumme ?? 0).toFixed(2)} €`}
          color="warning"
          sx={chipStyle}
        />
      </Box>

      {/* =====================================================
          ZAHLUNGSNACHWEISE
      ===================================================== */}

      <Card>
        <CardContent>
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              mb: spacing.card,
            }}
          >
            <Typography variant="h6">Zahlungsnachweise</Typography>

            <Button
              variant="contained"
              onClick={() => {
                setBearbeiteterZahlungsnachweis(null);
                setDialogOpen(true);
              }}
            >
              Neuer Zahlungsnachweis
            </Button>
          </Box>

          {zahlungsnachweise.length === 0 ? (
            <Alert severity="info">Es wurden noch keine Zahlungsnachweise erfasst.</Alert>
          ) : (
            <GenericTableTanstack<ZahlungsnachweisListDTO>
              data={zahlungsnachweise}
              columns={zahlungsnachweiseCols}
              loading={loading}
              height={250}
            />
          )}
        </CardContent>
      </Card>

      {/* =====================================================
          TEILNEHMERBEITRÄGE
      ===================================================== */}

      <Card>
        <CardContent
          sx={{
            p: padding.card,
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              mb: spacing.card,
            }}
          >
            <Typography
              variant="h6"
              sx={{
                fontSize: fontSize.sectionTitle,
              }}
            >
              Teilnehmerbeiträge
            </Typography>
          </Box>

          <GenericTableTanstack<TeilnehmerListDTO>
            data={data}
            columns={columns}
            loading={loading}
            height={600}
            mobileRenderRow={(row) => {
              const status = row.zahlungsstatus ?? "ROT";

              return (
                <Box>
                  {/* NAME + SOLL */}

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      gap: 1,
                    }}
                  >
                    <Typography
                      sx={{
                        fontWeight: 700,
                        fontSize: "1rem",
                        flex: 1,
                        minWidth: 0,
                      }}
                    >
                      {row.person.name}, {row.person.vorname}
                    </Typography>

                    <Typography
                      sx={{
                        fontWeight: 700,
                        color: "primary.main",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {getBeitrag(row).toFixed(2)} €
                    </Typography>
                  </Box>

                  {/* VEREIN + ALTER + STATUS */}

                  <Stack
                    direction="row"
                    alignItems="center"
                    justifyContent="space-between"
                    spacing={1}
                    sx={{ mt: 0.5 }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      {row.person.hauptvereinAbk ?? "-"}
                      {" • Alter: "}
                      {row.alterBeiBeginn ?? "-"}
                    </Typography>

                    <Chip
                      size="small"
                      label={zahlungsstatusLabel[status]}
                      color={zahlungsstatusColor[status]}
                    />
                  </Stack>

                  {/* GEZAHLTER BETRAG */}

                  {(row.gezahlterBetrag ?? 0) > 0 && (
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{
                        display: "block",
                        mt: 0.5,
                      }}
                    >
                      Gezahlt: {(row.gezahlterBetrag ?? 0).toFixed(2)} €
                    </Typography>
                  )}

                  {/* ROLLE */}

                  {row.rolle && (
                    <Box sx={{ mt: 0.5 }}>
                      <Chip size="small" label={row.rolle === "L" ? "Leiter" : "Mitarbeiter"} />
                    </Box>
                  )}
                </Box>
              );
            }}
          />
        </CardContent>
      </Card>

      {/* =====================================================
          NEUER ZAHLUNGSNACHWEIS
      ===================================================== */}

      <ZahlungsnachweisDialog
        open={dialogOpen}
        veranstaltungId={veranstaltungId}
        teilnehmer={data}
        zahlungsnachweis={bearbeiteterZahlungsnachweis}
        onClose={() => {
          setDialogOpen(false);

          setBearbeiteterZahlungsnachweis(null);
        }}
        onSave={async (dto) => {
          try {
            if (bearbeiteterZahlungsnachweis) {
              await apiClient.put(
                `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${bearbeiteterZahlungsnachweis.id}`,
                dto,
              );
            } else {
              await apiClient.post(`/veranstaltungen/${veranstaltungId}/zahlungsnachweise`, dto);
            }

            setDialogOpen(false);
            setBearbeiteterZahlungsnachweis(null);

            await load();
          } catch (err) {
            console.error(err);
            setError(
              bearbeiteterZahlungsnachweis
                ? "Zahlungsnachweis konnte nicht geändert werden."
                : "Zahlungsnachweis konnte nicht gespeichert werden.",
            );
          }
        }}
      />

      <DeleteConfirmDialog
        open={deleteDialogOpen}
        title="Zahlungsnachweis löschen"
        message="Soll dieser Zahlungsnachweis wirklich gelöscht werden?"
        onClose={() => {
          setDeleteDialogOpen(false);
          setDeleteZahlungsnachweisId(null);
        }}
        onConfirm={handleConfirmDeleteZahlungsnachweis}
      />
    </Stack>
  );
};

export default BeitraegePage;
