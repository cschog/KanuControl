import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import Money from "@/components/common/Money";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

import RueckzahlungTeilnehmerbeitragDialog from "@/components/finanzen/beitraege/RueckzahlungTeilnehmerbeitragDialog";

import {
  getOffeneUeberzahlungen,
  rueckzahlungTeilnehmerbeitrag,
} from "@/api/services/zahlungsnachweisApi";

import { OffeneUeberzahlungDTO } from "@/api/types/beitraege";

import BackFooter from "@/components/common/BackFooter";

import { zahlungsnachweiseColumns } from "@/components/finanzen/beitraege/zahlungsnachweiseColumns";
import ZahlungsnachweisDialog from "@/components/finanzen/beitraege/ZahlungsnachweisDialog";
import { fontSize, padding, chip, layout, spacing } from "@/theme/ui";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { beitraegeColumns } from "@/components/finanzen/beitraege/beitraegeColumns";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

import { upload } from "@/api/services/zahlungsnachweisApi";
import { optimizeUploadFile } from "@/utils/imageUtils";

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

  const [offeneUeberzahlungen, setOffeneUeberzahlungen] = useState<OffeneUeberzahlungDTO[]>([]);

  const [rueckzahlungDialogOpen, setRueckzahlungDialogOpen] = useState(false);

  const [selectedUeberzahlung, setSelectedUeberzahlung] = useState<OffeneUeberzahlungDTO | null>(
    null,
  );

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
    } catch (err: unknown) {
      console.error(err);
      setError(getApiErrorMessage(err));
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
    } catch (err: unknown) {
      console.error(err);
      setError(getApiErrorMessage(err));
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

    const [beitraegeResponse, ueberzahlungen] = await Promise.all([
      apiClient.get<TeilnehmerBeitraegeResponseDTO>(
        `/veranstaltungen/${veranstaltungId}/beitraege`,
      ),

      getOffeneUeberzahlungen(veranstaltungId),
    ]);

    setSummary(beitraegeResponse.data.summary);
    setZahlungsnachweise(beitraegeResponse.data.zahlungsnachweise);
    setData(beitraegeResponse.data.teilnehmer);

    setOffeneUeberzahlungen(ueberzahlungen);
  } catch (err: unknown) {
    console.error(err);
    setError(getApiErrorMessage(err));
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
              flexDirection: { xs: "column", sm: "row" },
              justifyContent: "space-between",
              alignItems: { xs: "stretch", sm: "center" },
              gap: 1,
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
              sx={{
                alignSelf: { xs: "flex-end", sm: "auto" },
                whiteSpace: "nowrap",
              }}
            >
              + Zahlungsnachweis
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
              mobileRenderRow={(row) => (
                <Box>
                  {/* DATUM + BETRAG */}

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
                      }}
                    >
                      {row.datum}
                    </Typography>

                    <Typography
                      sx={{
                        fontWeight: 700,
                        color: "primary.main",
                        whiteSpace: "nowrap",
                      }}
                    >
                      <Money value={row.betrag} />
                    </Typography>
                  </Box>

                  {/* TEILNEHMER */}

                  {row.rueckzahlung ? (
                    <Typography variant="body2" color="text.secondary">
                      Rückzahlung
                    </Typography>
                  ) : (
                    row.teilnehmer.length > 0 && (
                      <Box sx={{ mt: 0.75 }}>
                        <Typography variant="body2" color="text.secondary">
                          Teilnehmer:
                        </Typography>

                        <Stack spacing={0.25} sx={{ mt: 0.25 }}>
                          {row.teilnehmer.map((tn) => (
                            <Typography key={tn.id} variant="body2">
                              {tn.nachname}, {tn.vorname}
                            </Typography>
                          ))}
                        </Stack>
                      </Box>
                    )
                  )}

                  {/* DOKUMENTE + AKTIONEN */}

                  <Stack
                    direction="row"
                    alignItems="center"
                    justifyContent="space-between"
                    sx={{ mt: 0.75 }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      {"Dokumente: "}
                      {row.anzahlDokumente ?? 0}
                    </Typography>

                    <Stack direction="row" spacing={0}>
                      <Tooltip title="Bearbeiten">
                        <IconButton size="small" onClick={() => handleEditZahlungsnachweis(row.id)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>

                      <Tooltip title="Löschen">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleDeleteZahlungsnachweis(row.id)}
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Stack>
                  </Stack>
                </Box>
              )}
            />
          )}
        </CardContent>
      </Card>

      {/* =====================================================
    OFFENE ÜBERZAHLUNGEN
===================================================== */}

      {offeneUeberzahlungen.length > 0 && (
        <Card>
          <CardContent>
            <Stack spacing={2}>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Typography variant="h6">Offene Überzahlungen</Typography>

                <Chip label={`${offeneUeberzahlungen.length}`} color="warning" />
              </Box>

              <Alert severity="warning">
                Für diese Zahlungsnachweise wurden Beträge bezahlt, die noch keinem Teilnehmer
                zugeordnet und noch nicht zurückgezahlt wurden.
              </Alert>

              <Stack spacing={1}>
                {offeneUeberzahlungen.map((ueberzahlung) => (
                  <Card key={ueberzahlung.zahlungsnachweisId} variant="outlined">
                    <CardContent>
                      <Stack
                        direction={{ xs: "column", sm: "row" }}
                        justifyContent="space-between"
                        alignItems={{ xs: "stretch", sm: "center" }}
                        spacing={2}
                      >
                        <Box>
                          <Typography fontWeight={700}>Zahlung vom {ueberzahlung.datum}</Typography>

                          {ueberzahlung.bemerkung && (
                            <Typography variant="body2" color="text.secondary">
                              {ueberzahlung.bemerkung}
                            </Typography>
                          )}

                          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                            Ursprünglich: {ueberzahlung.urspruenglicherBetrag.toFixed(2)} €{" • "}
                            Zugeordnet: {ueberzahlung.zugeordnet.toFixed(2)} €{" • "}
                            Bereits zurückgezahlt: {ueberzahlung.bereitsZurueckgezahlt.toFixed(2)} €
                          </Typography>
                        </Box>

                        <Stack
                          direction={{ xs: "row", sm: "column" }}
                          spacing={1}
                          alignItems={{ xs: "center", sm: "flex-end" }}
                        >
                          <Typography fontWeight={700} color="warning.main">
                            <Money value={ueberzahlung.offeneUeberzahlung} />
                          </Typography>

                          <Button
                            variant="contained"
                            color="warning"
                            onClick={() => {
                              setSelectedUeberzahlung(ueberzahlung);

                              setRueckzahlungDialogOpen(true);
                            }}
                          >
                            Rückzahlung
                          </Button>
                        </Stack>
                      </Stack>
                    </CardContent>
                  </Card>
                ))}
              </Stack>
            </Stack>
          </CardContent>
        </Card>
      )}

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
        onSave={async (dto, files) => {
          try {
            let zahlungsnachweisId: number;

            if (bearbeiteterZahlungsnachweis) {
              // Bestehenden Zahlungsnachweis aktualisieren
              await apiClient.put(
                `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${bearbeiteterZahlungsnachweis.id}`,
                dto,
              );

              zahlungsnachweisId = bearbeiteterZahlungsnachweis.id;
            } else {
              // Neuen Zahlungsnachweis anlegen
              const response = await apiClient.post(
                `/veranstaltungen/${veranstaltungId}/zahlungsnachweise`,
                dto,
              );

              zahlungsnachweisId = response.data.id;

              // Dokumente erst NACH erfolgreicher Erstellung hochladen
              if (files.length > 0) {
                await Promise.all(
                  files.map(async (file) => {
                    const optimizedFile = await optimizeUploadFile(file);

                    return upload(veranstaltungId, zahlungsnachweisId, optimizedFile);
                  }),
                );
              }
            }

            setDialogOpen(false);
            setBearbeiteterZahlungsnachweis(null);

            await load();
          } catch (err: unknown) {
            console.error(err);
            setError(getApiErrorMessage(err));
          }
        }}
      />

      <RueckzahlungTeilnehmerbeitragDialog
        open={rueckzahlungDialogOpen}
        ueberzahlung={selectedUeberzahlung}
        onClose={() => {
          setRueckzahlungDialogOpen(false);
          setSelectedUeberzahlung(null);
        }}
        onSave={async (data) => {
          try {
            await rueckzahlungTeilnehmerbeitrag(veranstaltungId, data);

            setRueckzahlungDialogOpen(false);
            setSelectedUeberzahlung(null);

            await load();
          } catch (err: unknown) {
            console.error(err);

            setError(getApiErrorMessage(err));
          }
        }}
      />
      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
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
      <BackFooter
        label="Zurück zu Durchführung"
        path={`/veranstaltungen/${veranstaltungId}/finanzen/durchfuehrung`}
      />
    </Stack>
  );
};

export default BeitraegePage;
