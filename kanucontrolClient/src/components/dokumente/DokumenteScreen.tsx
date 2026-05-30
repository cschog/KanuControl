import React, { useEffect, useState } from "react";

import { Box, Typography, Paper, Button, Stack } from "@mui/material";

import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import VisibilityIcon from "@mui/icons-material/Visibility";
import DownloadIcon from "@mui/icons-material/Download";
import { Accordion, AccordionSummary, AccordionDetails } from "@mui/material";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { validateAbrechnung } from "@/api/services/abrechnungApi";
import { ValidationResult } from "@/api/types/ValidationResult";
import apiClient from "@/api/client/apiClient";

import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";

import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";

const DokumenteScreen: React.FC = () => {
  const navigate = useNavigate();
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);

  const [alterValidation, setAlterValidation] = useState<ValidationResult | null>(null);


  
  /* =========================================================
     Aktive Veranstaltung laden
     ========================================================= */

  useEffect(() => {
    (async () => {
      try {
        const v = await getActiveVeranstaltung();

        setVeranstaltung(v);

       if (v?.id) {
         const abrechnung = await validateAbrechnung(v.id);
         setAlterValidation(abrechnung);
       }
      } catch (err) {
        console.error("Keine aktive Veranstaltung gefunden", err);
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

            {renderSection("FM / JEM Antrag", "fm-jem-report", "fm-jem.pdf")}

            {/* Teilnehmerliste */}

            {renderSection("Teilnehmerliste", "teilnehmer/pdf", "teilnehmerliste.pdf")}

            {/* Abrechnung */}
            {renderValidationWarning("Abrechnung derzeit nicht möglich", alterValidation)}
            {renderSection(
              "Abrechnung",
              "abrechnung/pdf",
              "abrechnung.pdf",
              !alterValidation?.valid,
            )}

            {/* Erhebungsbogen */}

            {renderValidationWarning("Erhebungsbogen derzeit nicht möglich", alterValidation)}

            {renderSection(
              "Erhebungsbogen",
              "erhebungsbogen/pdf",
              "erhebungsbogen.pdf",
              !alterValidation?.valid,
            )}
          </Stack>
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
