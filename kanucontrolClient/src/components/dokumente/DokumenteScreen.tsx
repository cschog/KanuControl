import React, { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";

import { Box, Typography, Paper, Button, Stack, CircularProgress } from "@mui/material";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import VisibilityIcon from "@mui/icons-material/Visibility";
import DownloadIcon from "@mui/icons-material/Download";
import { Accordion, AccordionSummary, AccordionDetails } from "@mui/material";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { validateDokument } from "@/api/services/dokumentValidationApi";
import { PdfDokumentTyp } from "@/api/enums/PdfDokumentTyp";
import { ValidationResult } from "@/api/types/ValidationResult";
import apiClient from "@/api/client/apiClient";

import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";

import { VeranstaltungDetail } from "@/api/types/veranstaltung/VeranstaltungDetail";
import { ReisekostenPdfDialog } from "@/components/finanzen/reisekosten/ReisekostenPdfDialog";
import { radius } from "@/theme/ui";

const DokumenteScreen: React.FC = () => {
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);
  const [anmeldungValidation, setAnmeldungValidation] = useState<ValidationResult | null>(null);
  const [teilnehmerValidation, setTeilnehmerValidation] = useState<ValidationResult | null>(null);
  const [teilnehmerDatenkontrolleValidation, setTeilnehmerDatenkontrolleValidation] =
    useState<ValidationResult | null>(null);
  const [erhebungsbogenValidation, setErhebungsbogenValidation] = useState<ValidationResult | null>(
    null,
  );
  const [abrechnungValidation, setAbrechnungValidation] = useState<ValidationResult | null>(null);
  const [reisekostenOpen, setReisekostenOpen] = useState(false);
  const [loadingReport, setLoadingReport] = useState<string | null>(null);
  const [zahlungsnachweiseValidation, setZahlungsnachweiseValidation] =
    useState<ValidationResult | null>(null);

  const [error, setError] = useState<string | null>(null);

  const [belegeValidation, setBelegeValidation] = useState<ValidationResult | null>(null);

  const [fahrkostenValidation, setFahrkostenValidation] = useState<ValidationResult | null>(null);

  /* =========================================================
     Aktive Veranstaltung laden
     ========================================================= */

  useEffect(() => {
    (async () => {
      try {
        setError(null);

        const v = await getActiveVeranstaltung();

        setVeranstaltung(v);

        if (v?.id) {
          const [
            anmeldung,
            teilnehmerliste,
            teilnehmerDatenkontrolle,
            erhebungsbogen,
            abrechnung,
            zahlungsnachweise,
            belege,
            fahrkosten,
          ] = await Promise.all([
            validateDokument(v.id, PdfDokumentTyp.ANMELDUNG),
            validateDokument(v.id, PdfDokumentTyp.TEILNEHMERLISTE),
            validateDokument(v.id, PdfDokumentTyp.TEILNEHMER_DATENKONTROLLE),
            validateDokument(v.id, PdfDokumentTyp.ERHEBUNGSBOGEN),
            validateDokument(v.id, PdfDokumentTyp.ABRECHNUNG),
            validateDokument(v.id, PdfDokumentTyp.ZAHLUNGSNACHWEISE),
            validateDokument(v.id, PdfDokumentTyp.BELEGE),
            validateDokument(v.id, PdfDokumentTyp.REISEKOSTENABRECHNUNG),
          ]);

          setAnmeldungValidation(anmeldung);
          setTeilnehmerValidation(teilnehmerliste);
          setTeilnehmerDatenkontrolleValidation(teilnehmerDatenkontrolle);
          setErhebungsbogenValidation(erhebungsbogen);
          setAbrechnungValidation(abrechnung);
          setZahlungsnachweiseValidation(zahlungsnachweise);
          setBelegeValidation(belege);
          setFahrkostenValidation(fahrkosten);
        }
      } catch (err: unknown) {
        console.error("Fehler beim Laden der Dokumente:", err);
        setError(getApiErrorMessage(err));
      }
    })();
  }, []);

  /* =========================================================
     Generic PDF Preview
     ========================================================= */

  const handlePreview = async (endpoint: string) => {
    if (!veranstaltung?.id) return;

    setLoadingReport(endpoint);

    try {
      const res = await apiClient.get(`/veranstaltungen/${veranstaltung.id}/${endpoint}/view`, {
        responseType: "blob",
      });

      const blob = new Blob([res.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      window.open(url, "_blank");

      setTimeout(() => {
        window.URL.revokeObjectURL(url);
      }, 60_000);
    } catch (error: unknown) {
      console.error("PDF-Vorschau konnte nicht erstellt werden:", error);
      setError(getApiErrorMessage(error));
    } finally {
      setLoadingReport(null);
    }
  };

  /* =========================================================
     Generic PDF Download
     ========================================================= */

  const handleDownload = async (endpoint: string, fallbackFilename: string) => {
    if (!veranstaltung?.id) return;

    setLoadingReport(endpoint);

    try {
      const res = await apiClient.get(`/veranstaltungen/${veranstaltung.id}/${endpoint}/download`, {
        responseType: "blob",
      });

      const disposition = res.headers["content-disposition"];

      let filename = fallbackFilename;

      const match = disposition?.match(/filename="?([^";]+)"?/);

      if (match?.[1]) {
        filename = match[1];
      }

      const blob = new Blob([res.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");

      link.href = url;
      link.download = filename;

      document.body.appendChild(link);

      link.click();

      link.remove();

      window.URL.revokeObjectURL(url);
    } catch (error: unknown) {
      console.error("PDF konnte nicht heruntergeladen werden:", error);
      setError(getApiErrorMessage(error));
    } finally {
      setLoadingReport(null);
    }
  };

  /* =========================================================
     Reusable Report Section
     ========================================================= */
  const renderValidationWarning = (title: string, validation: ValidationResult | null) => {
    if (!validation || validation.valid) {
      return null;
    }

    return (
      <Accordion
        sx={{
          bgcolor: "#fff3cd",
          border: "1px solid #ffe69c",
          borderRadius: radius.dialog,
          boxShadow: "none",
        }}
      >
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography fontWeight={700}>⚠️ {title}</Typography>
        </AccordionSummary>

        <AccordionDetails>
          <Box component="ul" sx={{ mb: 0 }}>
            {validation.messages.map((m: string, i: number) => (
              <li key={i}>{m}</li>
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>
    );
  };

  const renderSection = (
    title: string,
    endpoint: string,
    fallbackFilename: string,
    disabled: boolean = false,
  ) => {
    const loading = loadingReport === endpoint;

    return (
      <Paper
        elevation={3}
        sx={{
          p: 3,
          borderRadius: radius.dialog,
        }}
      >
        <Stack direction="row" alignItems="center" spacing={1} mb={2}>
          <PictureAsPdfIcon />

          <Typography variant="h6">{title}</Typography>
        </Stack>

        <Stack
          direction={{
            xs: "column",
            sm: "row",
          }}
          spacing={2}
        >
          <Button
            variant="contained"
            startIcon={
              loading ? <CircularProgress size={18} color="inherit" /> : <VisibilityIcon />
            }
            onClick={() => void handlePreview(endpoint)}
            disabled={disabled || loading}
          >
            {loading ? "PDF wird erstellt ..." : "Vorschau"}
          </Button>

          <Button
            variant="outlined"
            startIcon={loading ? <CircularProgress size={18} /> : <DownloadIcon />}
            onClick={() => void handleDownload(endpoint, fallbackFilename)}
            disabled={disabled || loading}
          >
            {loading ? "Bitte warten ..." : "Download"}
          </Button>
        </Stack>

        {loading && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Der Bericht wird erstellt. Das kann etwas dauern ...
          </Typography>
        )}
      </Paper>
    );
  };

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box maxWidth={1000} mx="auto" mt={4} px={2}>
      <Typography variant="h4" gutterBottom>
        Dokumente
      </Typography>

      {veranstaltung ? (
        <>
          <Typography variant="body1" sx={{ mb: 4 }}>
            Aktive Veranstaltung: <b>{veranstaltung.name}</b>
          </Typography>

          <Grid container spacing={3}>
            {/* FM / JEM */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "FM / JEM Antrag nicht möglich - bitte aufklappen",
                anmeldungValidation,
              )}

              {renderSection(
                "FM / JEM Antrag",
                "fm-jem-report",
                "fm-jem.pdf",
                !anmeldungValidation?.valid,
              )}
            </Grid>

            {/* Teilnehmerliste */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Teilnehmerliste nicht möglich - bitte aufklappen",
                teilnehmerValidation,
              )}

              {renderSection(
                "Teilnehmerliste",
                "teilnehmer/pdf",
                "teilnehmerliste.pdf",
                !teilnehmerValidation?.valid,
              )}
            </Grid>

            {/* Teilnehmer-Datenkontrolle */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Teilnehmer-Datenkontrolle nicht möglich - bitte aufklappen",
                teilnehmerDatenkontrolleValidation,
              )}

              {renderSection(
                "Teilnehmerdaten prüfen",
                "teilnehmer/datenkontrolle/pdf",
                "teilnehmer-datenkontrolle.pdf",
                !teilnehmerDatenkontrolleValidation?.valid,
              )}
            </Grid>

            {/* Abrechnung */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Abrechnung nicht möglich - bitte aufklappen",
                abrechnungValidation,
              )}

              {renderSection(
                "Abrechnung",
                "abrechnung/pdf",
                "abrechnung.pdf",
                !abrechnungValidation?.valid,
              )}
            </Grid>

            {/* Erhebungsbogen */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Erhebungsbogen nicht möglich - bitte aufklappen",
                erhebungsbogenValidation,
              )}

              {renderSection(
                "Erhebungsbogen",
                "erhebungsbogen/pdf",
                "erhebungsbogen.pdf",
                !erhebungsbogenValidation?.valid,
              )}
            </Grid>

            {/* Zahlungsnachweise */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Zahlungsnachweise nicht möglich - bitte aufklappen",
                zahlungsnachweiseValidation,
              )}

              {renderSection(
                "Zahlungsnachweise",
                "zahlungsnachweise/pdf",
                "zahlungsnachweise.pdf",
                !zahlungsnachweiseValidation?.valid,
              )}
            </Grid>

            {/* Belege */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Belege nicht möglich - bitte aufklappen", belegeValidation)}

              {renderSection("Belege", "belege/pdf", "belege.pdf", !belegeValidation?.valid)}
            </Grid>

            {/* Fahrkosten */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Fahrkostenabrechnung nicht möglich - bitte aufklappen",
                fahrkostenValidation,
              )}

              <Paper
                elevation={3}
                sx={{
                  p: 3,
                  borderRadius: radius.dialog,
                }}
              >
                <Stack direction="row" alignItems="center" spacing={1} mb={2}>
                  <PictureAsPdfIcon />
                  <Typography variant="h6">Fahrkosten</Typography>
                </Stack>

                <Button
                  variant="contained"
                  onClick={() => setReisekostenOpen(true)}
                  disabled={!fahrkostenValidation?.valid}
                >
                  Fahrkosten auswählen
                </Button>
              </Paper>
            </Grid>

            {/* Finanzausgleich */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderSection("Finanzausgleich", "finanzausgleich/pdf", "finanzausgleich.pdf")}
            </Grid>
          </Grid>
          <ReisekostenPdfDialog
            open={reisekostenOpen}
            veranstaltungId={veranstaltung.id}
            onClose={() => setReisekostenOpen(false)}
          />
        </>
      ) : (
        <Paper
          sx={{
            p: 3,
            borderRadius: radius.dialog,
          }}
        >
          <Typography color="text.secondary">Keine aktive Veranstaltung gefunden</Typography>
        </Paper>
      )}
      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
};

export default DokumenteScreen;
