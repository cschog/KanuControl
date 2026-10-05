import React, { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";

import { Box, Typography, Paper, Button, Stack, CircularProgress } from "@mui/material";
import { ErrorDialog } from "@/core/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import VisibilityIcon from "@mui/icons-material/Visibility";
import DownloadIcon from "@mui/icons-material/Download";
import { Accordion, AccordionSummary, AccordionDetails } from "@mui/material";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { validateDokument } from "@/kjfp/api/services/dokumentValidationApi";
import { PdfDokumentTyp } from "@/kjfp/api/enums/PdfDokumentTyp";
import { ValidationResult } from "@/kjfp/types/ValidationResult";
import apiClient from "@/core/api/client/apiClient";

import { getActiveVeranstaltung } from "@/kjfp/api/services/veranstaltungApi";

import { VeranstaltungDetail } from "@/kjfp/types/veranstaltung/VeranstaltungDetail";
import { ReisekostenPdfDialog } from "@/kjfp/components/finanzen/reisekosten/ReisekostenPdfDialog";
import { radius } from "@/core/theme/ui";

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

    // Fenster direkt aus der Benutzeraktion heraus öffnen
    const previewWindow = window.open("", "_blank");

    if (!previewWindow) {
      setError(
        "Die PDF-Vorschau konnte nicht geöffnet werden. Bitte Popups für KanuControl erlauben.",
      );
      return;
    }

    setLoadingReport(endpoint);

    try {
      const res = await apiClient.get(`/veranstaltungen/${veranstaltung.id}/${endpoint}/view`, {
        responseType: "blob",
      });

      const blob = new Blob([res.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      // PDF in das bereits geöffnete Fenster laden
      previewWindow.location.href = url;

      // URL erst später freigeben
      setTimeout(() => {
        window.URL.revokeObjectURL(url);
      }, 60_000);
    } catch (error: unknown) {
      previewWindow.close();

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
    if (!validation) {
      return null;
    }

    const messages = [...validation.errors, ...validation.warnings];

    if (messages.length === 0) {
      return null;
    }

    const hasErrors = validation.errors.length > 0;

    const hasPdfEditableWarnings = validation.warnings.some((item) => item.editableInPdf);

    return (
      <Accordion
        sx={{
          bgcolor: hasErrors ? "#f8d7da" : "#fff3cd",
          border: hasErrors ? "1px solid #f5c2c7" : "1px solid #ffe69c",
          borderRadius: radius.dialog,
          boxShadow: "none",
          mb: 2,
        }}
      >
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography fontWeight={700}>
            {hasErrors ? "❌" : "⚠️"}{" "}
            {hasErrors
              ? `${title} nicht möglich – bitte aufklappen`
              : `Hinweise zum ${title} – bitte aufklappen`}
          </Typography>
        </AccordionSummary>

        <AccordionDetails>
          <Box component="ul" sx={{ mb: 0 }}>
            {messages.map((item, i) => (
              <li key={i}>
                <Typography component="span">{item.message}</Typography>
              </li>
            ))}
          </Box>

          {!hasErrors && hasPdfEditableWarnings && (
            <Typography variant="body2" sx={{ mt: 2, fontWeight: 600 }}>
              Die fehlenden Angaben müssen im PDF manuell ergänzt werden.
            </Typography>
          )}
        </AccordionDetails>
      </Accordion>
    );
  };

  const renderSection = (
    title: string,
    endpoint: string,
    fallbackFilename: string,
    validation?: ValidationResult | null,
  ) => {
    const loading = loadingReport === endpoint;

    const blocked = validation != null && validation.errors.length > 0;

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

        {!blocked && (
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
              disabled={loading}
            >
              {loading ? "PDF wird erstellt ..." : "Vorschau"}
            </Button>

            <Button
              variant="outlined"
              startIcon={loading ? <CircularProgress size={18} /> : <DownloadIcon />}
              onClick={() => void handleDownload(endpoint, fallbackFilename)}
              disabled={loading}
            >
              {loading ? "Bitte warten ..." : "Download"}
            </Button>
          </Stack>
        )}

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
              {renderValidationWarning("FM / JEM Antrag", anmeldungValidation)}

              {renderSection("FM / JEM Antrag", "fm-jem-report", "fm-jem.pdf", anmeldungValidation)}
            </Grid>

            {/* Teilnehmerliste */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Teilnehmerliste", teilnehmerValidation)}

              {renderSection(
                "Teilnehmerliste",
                "teilnehmer/pdf",
                "teilnehmerliste.pdf",
                teilnehmerValidation,
              )}
            </Grid>

            {/* Teilnehmer-Datenkontrolle */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning(
                "Teilnehmer-Datenkontrolle",
                teilnehmerDatenkontrolleValidation,
              )}

              {renderSection(
                "Teilnehmerdaten prüfen",
                "teilnehmer/datenkontrolle/pdf",
                "teilnehmer-datenkontrolle.pdf",
                teilnehmerDatenkontrolleValidation,
              )}
            </Grid>

            {/* Abrechnung */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Abrechnung", abrechnungValidation)}

              {renderSection(
                "Abrechnung",
                "abrechnung/pdf",
                "abrechnung.pdf",
                abrechnungValidation,
              )}
            </Grid>

            {/* Erhebungsbogen */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Erhebungsbogen", erhebungsbogenValidation)}

              {renderSection(
                "Erhebungsbogen",
                "erhebungsbogen/pdf",
                "erhebungsbogen.pdf",
                erhebungsbogenValidation,
              )}
            </Grid>

            {/* Zahlungsnachweise */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Zahlungsnachweise", zahlungsnachweiseValidation)}

              {renderSection(
                "Zahlungsnachweise",
                "zahlungsnachweise/pdf",
                "zahlungsnachweise.pdf",
                zahlungsnachweiseValidation,
              )}
            </Grid>

            {/* Belege */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Belege", belegeValidation)}

              {renderSection("Belege", "belege/pdf", "belege.pdf", belegeValidation)}
            </Grid>

            {/* Fahrkosten */}
            <Grid size={{ xs: 12, md: 6 }}>
              {renderValidationWarning("Fahrkostenabrechnung", fahrkostenValidation)}

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
                  disabled={!fahrkostenValidation || fahrkostenValidation.errors.length > 0}
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
