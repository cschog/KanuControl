import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import { useEffect, useMemo, useState } from "react";

import MoneyField from "@/components/common/MoneyField";

import {
  TeilnehmerListDTO,
  ZahlungsPositionDTO,
  ZahlungsnachweisDetailDTO,
} from "@/api/types/beitraege";

interface Props {
  open: boolean;
  veranstaltungId: number;
  teilnehmer: TeilnehmerListDTO[];

  zahlungsnachweis?: ZahlungsnachweisDetailDTO | null;

  onClose: () => void;

  onSave: (data: {
    datum: string;
    betrag: number;
    bemerkung: string;
    positionen: ZahlungsPositionDTO[];
  }) => void;
}

const ZahlungsnachweisDialog = ({ open, teilnehmer, zahlungsnachweis, onClose, onSave }: Props) => {
  const [datum, setDatum] = useState("");
  const [bemerkung, setBemerkung] = useState("");
  const [betrag, setBetrag] = useState<number | null>(null);

  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const [suche, setSuche] = useState("");

  const [dokumente] = useState<File[]>([]);

  /* =========================================================
     RESET / INITIALISIERUNG
  ========================================================= */

  useEffect(() => {
    if (!open) return;

    if (zahlungsnachweis) {
      // Bearbeiten
      setDatum(zahlungsnachweis.datum);
      setBemerkung(zahlungsnachweis.bemerkung ?? "");
      setBetrag(zahlungsnachweis.betrag);

      setSelectedIds(zahlungsnachweis.positionen.map((position) => position.teilnehmerId));
    } else {
      // Neu
      setDatum(new Date().toISOString().split("T")[0]);
      setBemerkung("");
      setBetrag(null);
      setSelectedIds([]);
    }

    setSuche("");
  }, [open, zahlungsnachweis]);

  /* =========================================================
     FILTER
  ========================================================= */

  const gefilterteTeilnehmer = useMemo(() => {
    const suchtext = suche.trim().toLowerCase();

    if (!suchtext) {
      return teilnehmer;
    }

    return teilnehmer.filter((t) => {
      const name = `${t.person?.name ?? ""} ${t.person?.vorname ?? ""}`.toLowerCase();

      const verein = t.person?.hauptvereinAbk?.toLowerCase() ?? "";

      return name.includes(suchtext) || verein.includes(suchtext);
    });
  }, [teilnehmer, suche]);

  /* =========================================================
     SELECTION
  ========================================================= */

  const alleGefiltertenAusgewaehlt =
    gefilterteTeilnehmer.length > 0 &&
    gefilterteTeilnehmer.every((t) => selectedIds.includes(t.id));

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelectedIds((prev) => [...new Set([...prev, ...gefilterteTeilnehmer.map((t) => t.id)])]);
    } else {
      const ids = new Set(gefilterteTeilnehmer.map((t) => t.id));

      setSelectedIds((prev) => prev.filter((id) => !ids.has(id)));
    }
  };

  const handleSelect = (id: number, checked: boolean) => {
    setSelectedIds((prev) => {
      if (checked) {
        return prev.includes(id) ? prev : [...prev, id];
      }

      return prev.filter((x) => x !== id);
    });
  };

  /* =========================================================
     SAVE
  ========================================================= */

  const handleSave = () => {
    if (!datum || betrag === null || betrag <= 0 || selectedIds.length === 0) {
      return;
    }

    const ausgewaehlteTeilnehmer = selectedIds
      .map((id) => teilnehmer.find((t) => t.id === id))
      .filter(
        (
          t,
        ): t is TeilnehmerListDTO & {
          id: number;
        } => t !== undefined && t.id !== undefined,
      );

    const positionen: ZahlungsPositionDTO[] = ausgewaehlteTeilnehmer.map((t) => ({
      id: -t.id,
      teilnehmerId: t.id,
      vorname: t.person.vorname,
      nachname: t.person.name,
      betrag: 0,
    }));

    onSave({
      datum,
      betrag,
      bemerkung,
      positionen,
    });
  };

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>
        {zahlungsnachweis ? "Zahlungsnachweis bearbeiten" : "Neuer Zahlungsnachweis"}
      </DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {/* =================================================
              DATUM
          ================================================= */}

          <TextField
            label="Datum"
            type="date"
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
            InputLabelProps={{
              shrink: true,
            }}
            fullWidth
          />

          {/* =================================================
              BETRAG
          ================================================= */}

          <MoneyField
            label="Betrag"
            value={betrag ?? ""}
            onChange={(value) => setBetrag(value === "" ? null : Number(value))}
          />

          {/* =================================================
              BEMERKUNG
          ================================================= */}

          <TextField
            label="Bemerkung"
            multiline
            minRows={3}
            value={bemerkung}
            onChange={(e) => setBemerkung(e.target.value)}
            fullWidth
          />

          <Divider sx={{ my: 1 }} />

          {/* =================================================
              TEILNEHMER
          ================================================= */}

          <Box>
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                mb: 1,
              }}
            >
              <Typography variant="h6">Teilnehmer</Typography>

              <Typography variant="body2" color="text.secondary">
                {selectedIds.length} ausgewählt
              </Typography>
            </Box>

            <TextField
              size="small"
              fullWidth
              label="Teilnehmer suchen"
              placeholder="Name oder Verein"
              value={suche}
              onChange={(e) => setSuche(e.target.value)}
              sx={{ mb: 1 }}
            />

            <Box
              sx={{
                border: 1,
                borderColor: "divider",
                borderRadius: 1,
                overflow: "hidden",
              }}
            >
              {/* KOPFZEILE */}

              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: "52px minmax(0, 1fr) 110px",
                  alignItems: "center",
                  minHeight: 48,
                  px: 1,
                  bgcolor: "action.hover",
                  borderBottom: 1,
                  borderColor: "divider",
                }}
              >
                <Checkbox
                  checked={alleGefiltertenAusgewaehlt}
                  indeterminate={selectedIds.length > 0 && !alleGefiltertenAusgewaehlt}
                  onChange={handleSelectAll}
                />

                <Typography fontWeight={700}>Teilnehmer</Typography>

                <Typography fontWeight={700} textAlign="right" sx={{ pr: 1 }}>
                  Soll
                </Typography>
              </Box>

              {/* TEILNEHMER */}

              {gefilterteTeilnehmer.length === 0 ? (
                <Alert severity="info" sx={{ borderRadius: 0 }}>
                  Keine Teilnehmer gefunden.
                </Alert>
              ) : (
                gefilterteTeilnehmer.map((t) => {
                  const selected = selectedIds.includes(t.id);

                  return (
                    <Box
                      key={t.id}
                      sx={{
                        display: "grid",
                        gridTemplateColumns: "52px minmax(0, 1fr) 110px",
                        alignItems: "center",
                        minHeight: 52,
                        px: 1,
                        borderBottom: 1,
                        borderColor: "divider",
                        bgcolor: selected ? "action.selected" : "background.paper",
                        "&:last-child": {
                          borderBottom: 0,
                        },
                      }}
                    >
                      <Checkbox
                        checked={selected}
                        onChange={(e) => handleSelect(t.id, e.target.checked)}
                      />

                      <Box
                        sx={{
                          minWidth: 0,
                          cursor: "pointer",
                        }}
                        onClick={() => handleSelect(t.id, !selected)}
                      >
                        <Typography fontWeight={selected ? 700 : 500} noWrap>
                          {t.person.name}, {t.person.vorname}
                        </Typography>

                        <Typography variant="caption" color="text.secondary" noWrap>
                          {t.person.hauptvereinAbk ?? "-"}
                          {" • "}
                          Alter: {t.alterBeiBeginn ?? "-"}
                        </Typography>
                      </Box>

                      <Typography textAlign="right" fontWeight={600} sx={{ pr: 1 }}>
                        {(t.sollBeitrag ?? 0).toFixed(2)} €
                      </Typography>
                    </Box>
                  );
                })
              )}
            </Box>

            {selectedIds.length === 0 && (
              <Alert severity="info" sx={{ mt: 1 }}>
                Bitte mindestens einen Teilnehmer auswählen.
              </Alert>
            )}

            {selectedIds.length > 0 && betrag !== null && (
              <Alert severity="info" sx={{ mt: 1 }}>
                Der Gesamtbetrag von <strong>{betrag.toFixed(2)} €</strong> wird entsprechend der
                offenen Sollbeträge auf die ausgewählten Teilnehmer verteilt.
              </Alert>
            )}
          </Box>

          <Divider sx={{ my: 1 }} />

          {/* =================================================
              DOKUMENTE
          ================================================= */}

          <Box>
            <Typography variant="h6" sx={{ mb: 1 }}>
              Dokumente
            </Typography>

            <Button variant="outlined" fullWidth>
              Dokument hinzufügen
            </Button>

            {dokumente.length === 0 && (
              <Alert severity="info" sx={{ mt: 1 }}>
                Es wurden noch keine Dokumente ausgewählt.
              </Alert>
            )}
          </Box>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!datum || betrag === null || betrag <= 0 || selectedIds.length === 0}
        >
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ZahlungsnachweisDialog;
