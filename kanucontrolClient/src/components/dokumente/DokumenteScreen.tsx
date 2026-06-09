import React, { useEffect, useState } from "react";

import { Box, Typography, Paper, Button, Stack } from "@mui/material";
import axios from "axios";

import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import VisibilityIcon from "@mui/icons-material/Visibility";
import DownloadIcon from "@mui/icons-material/Download";
import { Accordion, AccordionSummary, AccordionDetails } from "@mui/material";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { validateDokument } from "@/api/services/dokumentValidationApi";
import { PdfDokumentTyp } from "@/api/enums/PdfDokumentTyp";
import { ValidationResult } from "@/api/types/ValidationResult";
import apiClient from "@/api/client/apiClient";

import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";


import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { ReisekostenPdfDialog } from "@/components/finanzen/reisekosten/ReisekostenPdfDialog";

const DokumenteScreen: React.FC = () => {
  const navigate = useNavigate();
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);

 const [anmeldungValidation, setAnmeldungValidation] = useState<ValidationResult | null>(null);

 const [teilnehmerValidation, setTeilnehmerValidation] = useState<ValidationResult | null>(null);

 const [erhebungsbogenValidation, setErhebungsbogenValidation] = useState<ValidationResult | null>(
   null,
 );

 const [abrechnungValidation, setAbrechnungValidation] = useState<ValidationResult | null>(null);

  const [reisekostenOpen, setReisekostenOpen] = useState(false);
 

  /* =========================================================
     Aktive Veranstaltung laden
     ========================================================= */

useEffect(() => {
  (async () => {
    try {
      const v = await getActiveVeranstaltung();

      setVeranstaltung(v);

      if (v?.id) {
        const [anmeldung, teilnehmerliste, erhebungsbogen, abrechnung] = await Promise.all([
          validateDokument(v.id, PdfDokumentTyp.ANMELDUNG),
          validateDokument(v.id, PdfDokumentTyp.TEILNEHMERLISTE),
          validateDokument(v.id, PdfDokumentTyp.ERHEBUNGSBOGEN),
          validateDokument(v.id, PdfDokumentTyp.ABRECHNUNG),
        ]);

        setAnmeldungValidation(anmeldung);
        setTeilnehmerValidation(teilnehmerliste);
        setErhebungsbogenValidation(erhebungsbogen);
        setAbrechnungValidation(abrechnung);
      }
    } catch (err) {
      if (axios.isAxiosError(err)) {
        console.error("Status:", err.response?.status);
        console.error("Data:", err.response?.data);
      } else {
        console.error(err);
      }
    }
  })();
}, []);

  /* =========================================================
     Generic PDF Preview
     ========================================================= */

  const handlePreview = async (endpoint: string) => {
    if (!veranstaltung?.id) return;

    const res = await apiClient.get(`/veranstaltungen/${veranstaltung.id}/${endpoint}/view`, {
      responseType: "blob",
    });

    const blob = new Blob([res.data], {
      type: "application/pdf",
    });

    const url = window.URL.createObjectURL(blob);

    window.open(url, "_blank");
  };

  /* =========================================================
     Generic PDF Download
     ========================================================= */

  const handleDownload = async (endpoint: string, fallbackFilename: string) => {
    if (!veranstaltung?.id) return;

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
          borderRadius: 2,
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
  ) => (
    <Paper
      elevation={3}
      sx={{
        p: 3,
        borderRadius: 3,
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
          startIcon={<VisibilityIcon />}
          onClick={() => handlePreview(endpoint)}
          disabled={disabled}
        >
          Vorschau
        </Button>

        <Button
          variant="outlined"
          startIcon={<DownloadIcon />}
          onClick={() => handleDownload(endpoint, fallbackFilename)}
          disabled={disabled}
        >
          Download
        </Button>
      </Stack>
    </Paper>
  );

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

          <Stack spacing={3}>
            {/* FM / JEM */}
            {renderValidationWarning("FM / JEM Antrag derzeit nicht möglich", anmeldungValidation)}

            {renderSection(
              "FM / JEM Antrag",
              "fm-jem-report",
              "fm-jem.pdf",
              !anmeldungValidation?.valid,
            )}

            {/* Teilnehmerliste */}
            {renderValidationWarning("Teilnehmerliste derzeit nicht möglich", teilnehmerValidation)}

            {renderSection(
              "Teilnehmerliste",
              "teilnehmer/pdf",
              "teilnehmerliste.pdf",
              !teilnehmerValidation?.valid,
            )}

            {/* Abrechnung */}
            {renderValidationWarning("Abrechnung derzeit nicht möglich", abrechnungValidation)}

            {renderSection(
              "Abrechnung",
              "abrechnung/pdf",
              "abrechnung.pdf",
              !abrechnungValidation?.valid,
            )}

            {/* Erhebungsbogen */}
            {renderValidationWarning(
              "Erhebungsbogen derzeit nicht möglich",
              erhebungsbogenValidation,
            )}

            {renderSection(
              "Erhebungsbogen",
              "erhebungsbogen/pdf",
              "erhebungsbogen.pdf",
              !erhebungsbogenValidation?.valid,
            )}

            {/* Reisekosten */}
            <Paper
              elevation={3}
              sx={{
                p: 3,
                borderRadius: 3,
              }}
            >
              <Stack direction="row" alignItems="center" spacing={1} mb={2}>
                <PictureAsPdfIcon />
                <Typography variant="h6">Fahrkosten</Typography>
              </Stack>

              <Button variant="contained" onClick={() => setReisekostenOpen(true)}>
                Fahrkosten auswählen
              </Button>
            </Paper>
          </Stack>
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
            borderRadius: 3,
          }}
        >
          <Typography color="text.secondary">Keine aktive Veranstaltung gefunden</Typography>
        </Paper>
      )}
      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/startmenue"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
};

export default DokumenteScreen;
