import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Stack,
  Typography,
} from "@mui/material";

import { fontSize, padding, chip, layout, spacing } from "@/theme/ui";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { beitraegeColumns } from "@/components/finanzen/beitraege/beitraegeColumns";
import { TeilnehmerBeitraegeResponseDTO, TeilnehmerListDTO } from "@/api/types/beitraege";

import { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";

interface Props {
  veranstaltungId: number;
}

const BeitraegePage = ({ veranstaltungId }: Props) => {
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<TeilnehmerListDTO[]>([]);
  const hatOffene = data.some((t) => !t.bezahlt);
  const [confirmResetOpen, setConfirmResetOpen] = useState(false);

  const handleAlleBezahlt = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.currentTarget.blur();

    if (!hatOffene) {
      setConfirmResetOpen(true);
      return;
    }

    updateAlleBezahlt(true);
  };

  const handleConfirmReset = async () => {
    setConfirmResetOpen(false);
    await updateAlleBezahlt(false);
  };

  const handleCancelReset = () => {
    setConfirmResetOpen(false);
  };

  const updateAlleBezahlt = async (bezahlt: boolean) => {
    try {
      await apiClient.patch(`/veranstaltungen/${veranstaltungId}/beitraege`, { bezahlt });

      const heute = new Date().toISOString().split("T")[0];

      setData((prev) =>
        prev.map((t) => ({
          ...t,
          bezahlt,
          bezahltAm: bezahlt ? (t.bezahlt ? t.bezahltAm : heute) : null,
        })),
      );
    } catch (err) {
      console.error(err);
    }
  };

  /* =========================================================
     LOAD
     ========================================================= */

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);

        const response = await apiClient.get<TeilnehmerBeitraegeResponseDTO>(
          `/veranstaltungen/${veranstaltungId}/beitraege`,
        );

        setData(response.data.teilnehmer);
      } catch (err) {
        console.error(err);

        setError("Beiträge konnten nicht geladen werden.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [veranstaltungId]);

  /* =========================================================
     SUMMEN
     ========================================================= */

  const getBeitrag = (t: TeilnehmerListDTO) => t.effektiverBeitrag ?? 0;

  const summe = data.reduce((sum, t) => sum + getBeitrag(t), 0);

  const bezahltSumme = data.filter((t) => t.bezahlt).reduce((sum, t) => sum + getBeitrag(t), 0);

  const offenSumme = summe - bezahltSumme;

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

  const handleBezahltChange = async (id: number, checked: boolean) => {
    try {
      await apiClient.patch(`/veranstaltungen/${veranstaltungId}/beitraege/${id}`, {
        bezahlt: checked,
      });

      setData((prev) =>
        prev.map((t) =>
          t.id === id
            ? {
                ...t,
                bezahlt: checked,
                bezahltAm: checked ? new Date().toISOString().split("T")[0] : null,
              }
            : t,
        ),
      );
    } catch (err) {
      console.error(err);
    }
  };

  const columns = beitraegeColumns({
    onBezahltChange: handleBezahltChange,
    setData,
  });

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
        <Chip label={`Teilnehmer: ${data.length}`} color="primary" sx={chipStyle} />
        <Chip label={`Soll: ${summe.toFixed(2)} €`} color="info" sx={chipStyle} />
        <Chip label={`Bezahlt: ${bezahltSumme.toFixed(2)} €`} color="success" sx={chipStyle} />
        <Chip label={`Offen: ${offenSumme.toFixed(2)} €`} color="warning" sx={chipStyle} />
      </Box>

      {/* =====================================================
          TABELLE
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
            <Typography variant="h6" sx={{ fontSize: fontSize.sectionTitle }}>
              Teilnehmerbeiträge
            </Typography>

            <Button
              variant="contained"
              color={hatOffene ? "success" : "warning"}
              onClick={handleAlleBezahlt}
            >
              {hatOffene ? "Alle bezahlt" : "Alle unbezahlt"}
            </Button>
          </Box>

          <GenericTableTanstack<TeilnehmerListDTO>
            data={data}
            columns={columns}
            loading={loading}
            height={600}
            mobileRenderRow={(row) => (
              <Box>
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

                <Stack
                  direction="row"
                  alignItems="center"
                  justifyContent="space-between"
                  spacing={1}
                  sx={{ mt: 0.3 }}
                >
                  <Typography variant="body2" color="text.secondary">
                    {row.person.hauptvereinAbk ?? "-"}
                    {" • Alter: "}
                    {row.alterBeiBeginn ?? "-"}
                  </Typography>

                  <Stack direction="row" alignItems="center" spacing={0.3}>
                    <Checkbox
                      size="small"
                      checked={row.bezahlt}
                      onChange={(e) => handleBezahltChange(row.id, e.target.checked)}
                    />

                    <Typography variant="caption" color="text.secondary">
                      bezahlt
                    </Typography>
                  </Stack>
                </Stack>

                {row.rolle && (
                  <Box sx={{ mt: 0.5 }}>
                    <Chip size="small" label={row.rolle === "L" ? "Leiter" : "Mitarbeiter"} />
                  </Box>
                )}
              </Box>
            )}
          />
        </CardContent>
      </Card>

      <Dialog open={confirmResetOpen} onClose={handleCancelReset} maxWidth="xs" fullWidth>
        <DialogTitle>Beiträge zurücksetzen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Alle Teilnehmer werden als <strong>nicht bezahlt</strong> markiert. Das Zahlungsdatum
            wird bei allen Teilnehmern entfernt. Möchten Sie fortfahren?
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCancelReset}>Abbrechen</Button>

          <Button color="warning" variant="contained" onClick={handleConfirmReset}>
            Zurücksetzen
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default BeitraegePage;
