import {
  Alert,
  Box,
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Stack,

  TextField,
  Typography,
} from "@mui/material";

import { fontSize, padding, radius, spacing, iconSize } from "@/theme/ui";

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

    height: {
      xs: 42,
      md: 52,
    },

    borderRadius: {
      xs: 2,
      md: 3,
    },

    "& .MuiChip-label": {
      px: {
        xs: 1.5,
        md: 2,
      },
    },
  };

  /* =========================================================
     RENDER
     ========================================================= */

  if (loading) {
    return (
      <Box sx={{ p: 4 }}>
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
              bezahltAm: checked ? new Date().toISOString().split("T")[0] : undefined,
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
    <Stack spacing={2}>
      {/* =====================================================
          HEADER
          ===================================================== */}
      <Box
        sx={{
          display: "grid",

          gridTemplateColumns: {
            xs: "1fr 1fr",
            md: "repeat(4, 1fr)",
          },

          gap: 1.5,
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
          <Typography
            variant="h6"
            sx={{
              fontSize: fontSize.sectionTitle,
              mb: spacing.card,
            }}
          >
            Teilnehmerbeiträge
          </Typography>

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
    </Stack>
  );
};

export default BeitraegePage;
