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
  FormControlLabel,
  MenuItem,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import { useEffect, useMemo, useRef, useState } from "react";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import { getFinanzgruppen, FinanzGruppe } from "@/api/services/finanzgruppenApi";

import MoneyField from "@/components/common/MoneyField";
import ZahlungsnachweisDokumentPanel from "@/components/finanzen/beitraege/ZahlungsnachweisDokumentPanel";
import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";

import {
  TeilnehmerListDTO,
  ZahlungsPositionDTO,
  ZahlungsnachweisDetailDTO,
  Zahlungsweg,
} from "@/api/types/beitraege";

interface Props {
  open: boolean;
  veranstaltungId: number;
  teilnehmer: TeilnehmerListDTO[];

  zahlungsnachweis?: ZahlungsnachweisDetailDTO | null;

  onClose: () => void;

  onSave: (
    data: {
      datum: string;
      betrag: number;
      zahlungsweg: Zahlungsweg | null;
      finanzGruppeId: number | null;
      bemerkung: string;
      positionen: ZahlungsPositionDTO[];
    },
    files: File[],
    referenzObjekt: ReferenzObjekt,
  ) => void | Promise<void>;
}

const REFERENZ_STORAGE_KEY = "kanucontrol.dokument.referenzObjekt";

const ZahlungsnachweisDialog = ({
  open,
  veranstaltungId,
  teilnehmer,
  zahlungsnachweis,
  onClose,
  onSave,
}: Props) => {
  const [datum, setDatum] = useState("");
  const [bemerkung, setBemerkung] = useState("");
  const [betrag, setBetrag] = useState<number | null>(null);
  const [zahlungsweg, setZahlungsweg] = useState<Zahlungsweg | null>(null);
  const [finanzgruppen, setFinanzgruppen] = useState<FinanzGruppe[]>([]);
  const [finanzGruppeId, setFinanzGruppeId] = useState<number | null>(null);
  const finanzGruppeFehlt = zahlungsweg === "QUITTUNG" && finanzGruppeId === null;

  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const [suche, setSuche] = useState("");
  const [dokumente, setDokumente] = useState<File[]>([]);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [saving, setSaving] = useState(false);

  const [referenzObjekt, setReferenzObjekt] = useState<ReferenzObjekt>(() => {
    const gespeichert = localStorage.getItem(REFERENZ_STORAGE_KEY);

    if (gespeichert && Object.values(ReferenzObjekt).includes(gespeichert as ReferenzObjekt)) {
      return gespeichert as ReferenzObjekt;
    }

    return ReferenzObjekt.DIN_A6;
  });

  /*
   * =========================================================
   * RESET / INITIALISIERUNG
   * =========================================================
   */

  useEffect(() => {
    if (!open) return;

    if (zahlungsnachweis) {
      setDatum(zahlungsnachweis.datum);
      setBemerkung(zahlungsnachweis.bemerkung ?? "");
      setBetrag(zahlungsnachweis.betrag);
      setZahlungsweg(zahlungsnachweis.zahlungsweg ?? null);
      setFinanzGruppeId(zahlungsnachweis.finanzGruppeId ?? null);

      setSelectedIds(
        zahlungsnachweis.positionen
          .map((p) => p.teilnehmerId)
          .filter((id): id is number => id !== undefined),
      );
    } else {
      setDatum(new Date().toISOString().split("T")[0]);
      setBemerkung("");
      setBetrag(null);
      setZahlungsweg(null);
      setFinanzGruppeId(null);
      setSelectedIds([]);
    }

    setDokumente([]);
    setSuche("");
  }, [open, zahlungsnachweis]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const loadFinanzgruppen = async () => {
      try {
        const gruppen = await getFinanzgruppen(veranstaltungId);
        setFinanzgruppen(gruppen);
      } catch (error) {
        console.error("Fehler beim Laden der Konten", error);
        setFinanzgruppen([]);
      }
    };

    void loadFinanzgruppen();
  }, [open, veranstaltungId]);

  useEffect(() => {
    if (zahlungsweg === "UEBERWEISUNG") {
      const vfg = finanzgruppen.find((gruppe) => gruppe.kuerzel === "VK");

      setFinanzGruppeId(vfg?.id ?? null);
    }
  }, [zahlungsweg, finanzgruppen]);

  /*
   * =========================================================
   * FILTER
   * =========================================================
   */

  const bestehendeTeilnehmerIds = useMemo(() => {
    return new Set(
      zahlungsnachweis?.positionen
        ?.map((p) => p.teilnehmerId)
        .filter((id): id is number => id !== undefined) ?? [],
    );
  }, [zahlungsnachweis]);

  const gefilterteTeilnehmer = useMemo(() => {
    const suchtext = suche.trim().toLowerCase();

    const verfuegbareTeilnehmer = teilnehmer.filter((t) => {
      if (zahlungsnachweis && bestehendeTeilnehmerIds.has(t.id)) {
        return true;
      }

      return t.zahlungsstatus !== "GRUEN";
    });

    if (!suchtext) {
      return verfuegbareTeilnehmer;
    }

    return verfuegbareTeilnehmer.filter((t) => {
      const name = `${t.person?.name ?? ""} ${t.person?.vorname ?? ""}`.toLowerCase();

      const verein = t.person?.hauptvereinAbk?.toLowerCase() ?? "";

      return name.includes(suchtext) || verein.includes(suchtext);
    });
  }, [teilnehmer, suche, zahlungsnachweis, bestehendeTeilnehmerIds]);

  /*
   * =========================================================
   * SELECTION
   * =========================================================
   */

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

  /*
   * =========================================================
   * DATEIEN
   * =========================================================
   */

  const handleFilesSelected = (selectedFiles: File[]) => {
    if (selectedFiles.length === 0) {
      return;
    }

    setDokumente((prev) => [...prev, ...selectedFiles]);
  };

  /*
   * =========================================================
   * SAVE
   * =========================================================
   */

  const handleSave = async () => {
    if (
      saving ||
      !datum ||
      betrag === null ||
      betrag <= 0 ||
      zahlungsweg === null ||
      finanzGruppeFehlt ||
      selectedIds.length === 0
    ) {
      return;
    }

    setSaving(true);

    try {
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

      await onSave(
        {
          datum,
          betrag,
          zahlungsweg,
          finanzGruppeId,
          bemerkung,
          positionen,
        },
        dokumente,
        referenzObjekt,
      );
    } finally {
      setSaving(false);
    }
  };

  /*
   * =========================================================
   * RENDER
   * =========================================================
   */

  return (
    <>
      <Dialog
        open={open}
        onClose={onClose}
        fullWidth
        maxWidth="md"
        fullScreen={false}
        slotProps={{
          paper: {
            sx: {
              m: {
                xs: 1,
                sm: 2,
              },
              width: {
                xs: "calc(100% - 16px)",
                sm: "auto",
              },
              maxHeight: {
                xs: "calc(100% - 16px)",
                sm: "calc(100% - 64px)",
              },
            },
          },
        }}
      >
        <DialogTitle
          sx={{
            fontSize: {
              xs: "1.35rem",
              sm: "1.5rem",
            },
            pb: 1,
          }}
        >
          {zahlungsnachweis ? "Zahlungsnachweis bearbeiten" : "Neuer Zahlungsnachweis"}
        </DialogTitle>

        <DialogContent
          sx={{
            overflowY: "auto",
            px: {
              xs: 1.5,
              sm: 3,
            },
          }}
        >
          <Stack spacing={2} sx={{ mt: 1 }}>
            {/* DATUM */}

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

            {/* BETRAG */}

            <MoneyField
              label="Betrag"
              value={betrag ?? ""}
              onChange={(value) => setBetrag(value === "" ? null : Number(value))}
            />

            {/* ZAHLUNGSWEG */}

            <TextField
              select
              label="Zahlungsweg"
              value={zahlungsweg ?? ""}
              onChange={(e) =>
                setZahlungsweg(e.target.value === "" ? null : (e.target.value as Zahlungsweg))
              }
              required
              fullWidth
            >
              <MenuItem value="UEBERWEISUNG">Überweisung</MenuItem>

              <MenuItem value="QUITTUNG">Quittung</MenuItem>
            </TextField>

            {/* Konto */}

            <TextField
              select
              label="Konto"
              value={finanzGruppeId ?? ""}
              onChange={(e) =>
                setFinanzGruppeId(e.target.value === "" ? null : Number(e.target.value))
              }
              fullWidth
              disabled={zahlungsweg === "UEBERWEISUNG"}
              required
            >
              {finanzgruppen
                .filter((gruppe) => {
                  // VK ist ausschließlich für Überweisungen
                  if (gruppe.kuerzel === "VK") {
                    return zahlungsweg === "UEBERWEISUNG";
                  }

                  // Systemgruppen nicht manuell auswählen
                  return !gruppe.system;
                })
                .map((gruppe) => (
                  <MenuItem key={gruppe.id} value={gruppe.id}>
                    {gruppe.kuerzel}
                  </MenuItem>
                ))}
            </TextField>

            {/* BEMERKUNG */}

            <TextField
              label="Bemerkung"
              multiline
              minRows={3}
              value={bemerkung}
              onChange={(e) => setBemerkung(e.target.value)}
              fullWidth
            />

            {/* DOKUMENTE */}

            <Divider sx={{ my: 1 }} />

            {zahlungsnachweis?.id ? (
              <ZahlungsnachweisDokumentPanel
                veranstaltungId={veranstaltungId}
                zahlungsnachweisId={zahlungsnachweis.id}
              />
            ) : (
              <Box>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                  mb={1}
                  gap={2}
                >
                  <Typography variant="h6">Dokumente</Typography>

                  <Stack direction="row" alignItems="center" spacing={2}>
                    <RadioGroup
                      row
                      value={referenzObjekt}
                      onChange={(event) => {
                        const value = event.target.value as ReferenzObjekt;

                        setReferenzObjekt(value);

                        localStorage.setItem(REFERENZ_STORAGE_KEY, value);
                      }}
                    >
                      <FormControlLabel
                        value={ReferenzObjekt.DIN_A7}
                        control={<Radio size="small" />}
                        label="A7"
                      />

                      <FormControlLabel
                        value={ReferenzObjekt.DIN_A6}
                        control={<Radio size="small" />}
                        label="A6"
                      />

                      <FormControlLabel
                        value={ReferenzObjekt.DIN_A5}
                        control={<Radio size="small" />}
                        label="A5"
                      />

                      <FormControlLabel
                        value={ReferenzObjekt.DIN_A4}
                        control={<Radio size="small" />}
                        label="A4"
                      />
                    </RadioGroup>

                    <Button
                      variant="outlined"
                      startIcon={<UploadFileIcon />}
                      onClick={() => fileInputRef.current?.click()}
                    >
                      Dokument hinzufügen
                    </Button>

                    <input
                      hidden
                      type="file"
                      accept=".pdf,image/*"
                      ref={fileInputRef}
                      multiple
                      onChange={(e) => {
                        const files = Array.from(e.target.files ?? []);

                        handleFilesSelected(files);

                        e.target.value = "";
                      }}
                    />
                  </Stack>
                </Stack>

                {dokumente.length === 0 ? (
                  <Alert severity="info">
                    Dokumente können hier bereits ausgewählt werden und werden zusammen mit dem
                    Zahlungsnachweis hochgeladen.
                  </Alert>
                ) : (
                  <Stack spacing={1}>
                    {dokumente.map((file, index) => (
                      <Box
                        key={`${file.name}-${index}`}
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          p: 1,
                          border: 1,
                          borderColor: "divider",
                          borderRadius: 1,
                        }}
                      >
                        <Box
                          sx={{
                            minWidth: 0,
                          }}
                        >
                          <Typography noWrap>{file.name}</Typography>

                          <Typography variant="caption" color="text.secondary">
                            {(file.size / 1024 / 1024).toFixed(1)} MB
                          </Typography>
                        </Box>

                        <Button
                          color="error"
                          size="small"
                          onClick={() => {
                            setDokumente((prev) => prev.filter((_, i) => i !== index));
                          }}
                        >
                          Entfernen
                        </Button>
                      </Box>
                    ))}
                  </Stack>
                )}
              </Box>
            )}

            <Divider sx={{ my: 1 }} />

            {/* TEILNEHMER */}

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
                    gridTemplateColumns: {
                      xs: "42px minmax(0, 1fr) 88px",
                      sm: "52px minmax(0, 1fr) 110px",
                    },
                    alignItems: "center",
                    minHeight: {
                      xs: 44,
                      sm: 48,
                    },
                    px: {
                      xs: 0.5,
                      sm: 1,
                    },
                    bgcolor: "action.hover",
                    borderBottom: 1,
                    borderColor: "divider",
                  }}
                >
                  <Checkbox
                    size="small"
                    checked={alleGefiltertenAusgewaehlt}
                    indeterminate={selectedIds.length > 0 && !alleGefiltertenAusgewaehlt}
                    onChange={handleSelectAll}
                  />

                  <Typography
                    fontWeight={700}
                    sx={{
                      fontSize: {
                        xs: "0.85rem",
                        sm: "1rem",
                      },
                    }}
                  >
                    Teilnehmer
                  </Typography>

                  <Typography
                    fontWeight={700}
                    textAlign="right"
                    sx={{
                      pr: {
                        xs: 0.5,
                        sm: 1,
                      },
                      fontSize: {
                        xs: "0.8rem",
                        sm: "1rem",
                      },
                    }}
                  >
                    Offen
                  </Typography>
                </Box>

                {/* TEILNEHMER */}

                {gefilterteTeilnehmer.length === 0 ? (
                  <Alert
                    severity="info"
                    sx={{
                      borderRadius: 0,
                    }}
                  >
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
                          gridTemplateColumns: {
                            xs: "42px minmax(0, 1fr) 88px",
                            sm: "52px minmax(0, 1fr) 110px",
                          },
                          alignItems: "center",
                          minHeight: {
                            xs: 44,
                            sm: 52,
                          },
                          px: {
                            xs: 0.5,
                            sm: 1,
                          },
                          borderBottom: 1,
                          borderColor: "divider",
                          bgcolor: selected ? "action.selected" : "background.paper",
                          "&:last-child": {
                            borderBottom: 0,
                          },
                        }}
                      >
                        <Checkbox
                          size="small"
                          checked={selected}
                          onChange={(e) => handleSelect(t.id, e.target.checked)}
                        />

                        <Box
                          sx={{
                            minWidth: 0,
                            cursor: "pointer",
                            py: 0.25,
                          }}
                          onClick={() => handleSelect(t.id, !selected)}
                        >
                          <Typography
                            fontWeight={selected ? 700 : 500}
                            noWrap
                            sx={{
                              fontSize: {
                                xs: "0.9rem",
                                sm: "1rem",
                              },
                              lineHeight: 1.2,
                            }}
                          >
                            {t.person.name}, {t.person.vorname}
                          </Typography>

                          <Typography
                            color="text.secondary"
                            noWrap
                            sx={{
                              fontSize: {
                                xs: "0.7rem",
                                sm: "0.75rem",
                              },
                              lineHeight: 1.1,
                            }}
                          >
                            {t.person.hauptvereinAbk ?? "-"}
                            {" • "}
                            Alter: {t.alterBeiBeginn ?? "-"}
                          </Typography>
                        </Box>

                        <Typography
                          textAlign="right"
                          fontWeight={600}
                          sx={{
                            pr: {
                              xs: 0.5,
                              sm: 1,
                            },
                            fontSize: {
                              xs: "0.85rem",
                              sm: "1rem",
                            },
                            whiteSpace: "nowrap",
                          }}
                        >
                          {((t.sollBeitrag ?? 0) - (t.gezahlterBetrag ?? 0)).toFixed(2)} €
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
          </Stack>
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose}>Abbrechen</Button>

          <Button
            variant="contained"
            onClick={() => void handleSave()}
            disabled={
              saving ||
              !datum ||
              betrag === null ||
              betrag <= 0 ||
              zahlungsweg === null ||
              finanzGruppeFehlt ||
              selectedIds.length === 0
            }
          >
            {saving ? "Speichern..." : "Speichern"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default ZahlungsnachweisDialog;
