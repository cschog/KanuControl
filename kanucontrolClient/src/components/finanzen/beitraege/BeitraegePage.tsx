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

  const [individuelleGebuehren, setIndividuelleGebuehren] = useState(false);

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

        setIndividuelleGebuehren(response.data.individuelleGebuehren);
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

  const getBeitrag = (t: TeilnehmerListDTO) => t.individuellerBeitrag ?? t.effektiverBeitrag ?? 0;

  const summe = data.reduce((sum, t) => sum + getBeitrag(t), 0);

  const bezahltSumme = data.filter((t) => t.bezahlt).reduce((sum, t) => sum + getBeitrag(t), 0);

  const offenSumme = summe - bezahltSumme;

  const chipStyle = {
    fontSize: {
      xs: "0.9rem",
      md: "1.3rem",
    },

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

  const handleBeitragSave = async (id: number, beitrag?: number) => {
    try {
      await apiClient.patch(`/veranstaltungen/${veranstaltungId}/beitraege/${id}/betrag`, {
        individuellerBeitrag: beitrag,
      });
    } catch (err) {
      console.error(err);
    }
  };

  const columns = beitraegeColumns({
    individuelleGebuehren,
    onBezahltChange: handleBezahltChange,
    onBeitragChange: handleBeitragSave,
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
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>
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

                  {individuelleGebuehren ? (
                    <TextField
                      type="number"
                      size="small"
                      value={row.individuellerBeitrag ?? row.effektiverBeitrag}
                      onChange={(e) => {
                        const value = e.target.value;

                        setData((prev) =>
                          prev.map((x) =>
                            x.id === row.id
                              ? {
                                  ...x,
                                  individuellerBeitrag: value === "" ? undefined : Number(value),
                                }
                              : x,
                          ),
                        );
                      }}
                      onBlur={() => handleBeitragSave(row.id, row.individuellerBeitrag)}
                      sx={{
                        width: 72,

                        "& input": {
                          textAlign: "right",
                          fontWeight: 700,
                          fontSize: "1rem",
                          paddingRight: "6px",
                        },
                      }}
                    />
                  ) : (
                    <Typography
                      sx={{
                        fontWeight: 700,
                        color: "primary.main",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {row.effektiverBeitrag.toFixed(2)} €
                    </Typography>
                  )}
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
