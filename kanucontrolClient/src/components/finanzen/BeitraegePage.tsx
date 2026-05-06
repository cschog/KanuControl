import {
  Alert,
  Box,
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";

import { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";

type TeilnehmerRolle = "L" | "M" | null;

interface PersonRefDTO {
  id: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
}

interface TeilnehmerListDTO {
  id: number;
  personId: number;
  person: PersonRefDTO;
  rolle: TeilnehmerRolle;
  individuellerBeitrag?: number;
  bezahlt: boolean;
  bezahltAm?: string;
  alterBeiBeginn?: number;
  effektiverBeitrag: number;
}

interface TeilnehmerBeitraegeResponseDTO {
  individuelleGebuehren: boolean;

  teilnehmer: TeilnehmerListDTO[];
}

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
    fontSize: "1.1rem",
    fontWeight: "bold",
    height: 52,
    borderRadius: 3,

    "& .MuiChip-label": {
      px: 2,
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

  return (
    <Stack spacing={2}>
      {/* =====================================================
          HEADER
          ===================================================== */}
      <Stack direction="row" spacing={2} flexWrap="wrap">
        <Chip label={`Teilnehmer: ${data.length}`} color="primary" sx={chipStyle} />

        <Chip label={`Soll: ${summe.toFixed(2)} €`} color="info" sx={chipStyle} />

        <Chip label={`Bezahlt: ${bezahltSumme.toFixed(2)} €`} color="success" sx={chipStyle} />

        <Chip label={`Offen: ${offenSumme.toFixed(2)} €`} color="warning" sx={chipStyle} />
      </Stack>

      {/* =====================================================
          TABELLE
          ===================================================== */}

      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Teilnehmerbeiträge
          </Typography>

          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: "bold" }}>Teilnehmer</TableCell>

                <TableCell align="center" sx={{ fontWeight: "bold" }}>
                  Alter
                </TableCell>

                <TableCell sx={{ fontWeight: "bold" }}>Verein</TableCell>

                <TableCell sx={{ fontWeight: "bold" }}>Rolle</TableCell>

                <TableCell align="right" sx={{ fontWeight: "bold" }}>
                  Beitrag
                </TableCell>

                <TableCell align="center" sx={{ fontWeight: "bold" }}>
                  bezahlt
                </TableCell>

                <TableCell sx={{ fontWeight: "bold" }}>bezahlt am</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {data.map((t) => (
                <TableRow key={t.id} hover>
                  <TableCell>
                    {t.person.name}, {t.person.vorname}
                  </TableCell>

                  <TableCell align="center">{t.alterBeiBeginn ?? "-"}</TableCell>

                  <TableCell>{t.person.hauptvereinAbk ?? "-"}</TableCell>

                  <TableCell>
                    {t.rolle === "L" && <Chip size="small" label="Leiter" color="secondary" />}

                    {t.rolle === "M" && <Chip size="small" label="Mitarbeiter" color="default" />}
                  </TableCell>

                  <TableCell align="right">
                    {individuelleGebuehren ? (
                      <TextField
                        type="number"
                        size="small"
                        value={t.individuellerBeitrag ?? t.effektiverBeitrag ?? ""}
                        onChange={(e) => {
                          const value = e.target.value;

                          setData((prev) =>
                            prev.map((x) =>
                              x.id === t.id
                                ? {
                                    ...x,
                                    individuellerBeitrag: value === "" ? undefined : Number(value),
                                  }
                                : x,
                            ),
                          );
                        }}
                        onBlur={() => handleBeitragSave(t.id, t.individuellerBeitrag)}
                        sx={{
                          width: 90,
                        }}
                      />
                    ) : (
                      <>{t.effektiverBeitrag.toFixed(2)} €</>
                    )}
                  </TableCell>

                  <TableCell align="center">
                    <Checkbox
                      checked={t.bezahlt}
                      onChange={(e) => handleBezahltChange(t.id, e.target.checked)}
                    />
                  </TableCell>

                  <TableCell>{t.bezahltAm ?? "-"}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Stack>
  );
};

export default BeitraegePage;
