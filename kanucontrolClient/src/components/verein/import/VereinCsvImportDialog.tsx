import React, { useRef, useState } from "react";

import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Switch,
  FormControlLabel,
  Alert,
  Typography,
  Stack,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TableContainer,
  Paper,
} from "@mui/material";

import apiClient from "@/api/client/apiClient";
import { CsvImportReport } from "@/api/types/CsvImportReport";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

interface Props {
  open: boolean;
  vereinId: number;
  onClose: () => void;
}

export function VereinCsvImportDialog({ open, vereinId, onClose }: Props) {
  /* ========================================================= */
  /* STATE */
  /* ========================================================= */

  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [mappingFile, setMappingFile] = useState<File | null>(null);

  const [dryRun, setDryRun] = useState(true);

  const [report, setReport] = useState<CsvImportReport | null>(null);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState<string | null>(null);

  /* ========================================================= */
  /* REFS */
  /* ========================================================= */

  const csvInputRef = useRef<HTMLInputElement>(null);

  const mappingInputRef = useRef<HTMLInputElement>(null);

  /* ========================================================= */
  /* MAPPING TEMPLATE */
  /* ========================================================= */

  const downloadMappingTemplate = () => {
    window.open("/api/csv-import/mapping-template", "_blank");
  };

  /* ========================================================= */
  /* DATEIAUSWAHL */
  /* ========================================================= */

  const handleCsvFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;

    setCsvFile(file);

    // Alter Prüfbericht gehört nicht mehr zur neuen Datei
    setReport(null);

    // Alten Fehler zurücksetzen
    setError(null);
  };

  const handleMappingFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;

    setMappingFile(file);

    // Alter Prüfbericht gehört möglicherweise nicht mehr
    // zum neuen Mapping
    setReport(null);

    // Alten Fehler zurücksetzen
    setError(null);
  };

  /* ========================================================= */
  /* IMPORT */
  /* ========================================================= */

  const handleImport = async () => {
    if (!csvFile) {
      return;
    }

    const form = new FormData();

    form.append("csv", csvFile);

    if (mappingFile) {
      form.append("mapping", mappingFile);
    }

    form.append("dryRun", String(dryRun));

    try {
      setLoading(true);

      setError(null);

      const { data } = await apiClient.post<CsvImportReport>(
        `/csv-import/verein/${vereinId}`,
        form,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        },
      );

      setReport(data);
    } catch (err: unknown) {
      console.error("Fehler beim CSV-Import", err);

      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (loading) return;

    setCsvFile(null);
    setMappingFile(null);
    setReport(null);
    setError(null);
    setDryRun(true);

    // Damit dieselbe Datei direkt erneut ausgewählt werden kann
    if (csvInputRef.current) {
      csvInputRef.current.value = "";
    }

    if (mappingInputRef.current) {
      mappingInputRef.current.value = "";
    }

    onClose();
  };

  /* ========================================================= */
  /* UI */
  /* ========================================================= */

  return (
    <>
      <Dialog open={open} onClose={loading ? undefined : handleClose} maxWidth="md" fullWidth>
        <DialogTitle>CSV-Import Mitglieder</DialogTitle>

        <DialogContent>
          <Stack spacing={2}>
            {/* ================================================= */}
            {/* MAPPING TEMPLATE */}
            {/* ================================================= */}

            <Button variant="outlined" onClick={downloadMappingTemplate} disabled={loading}>
              Mapping-Template herunterladen
            </Button>

            {/* ================================================= */}
            {/* CSV-DATEI */}
            {/* ================================================= */}

            <input
              ref={csvInputRef}
              type="file"
              accept=".csv,text/csv"
              hidden
              onChange={handleCsvFileChange}
            />

            <Button
              variant="outlined"
              color={csvFile ? "success" : "primary"}
              disabled={loading}
              onClick={() => csvInputRef.current?.click()}
            >
              CSV-Datei auswählen
            </Button>

            {csvFile && <Typography variant="caption">Ausgewählt: {csvFile.name}</Typography>}

            {/* ================================================= */}
            {/* MAPPING-DATEI */}
            {/* ================================================= */}

            <input
              ref={mappingInputRef}
              type="file"
              accept=".csv,text/csv"
              hidden
              onChange={handleMappingFileChange}
            />

            <Button
              variant="outlined"
              color={mappingFile ? "success" : "primary"}
              disabled={loading}
              onClick={() => mappingInputRef.current?.click()}
            >
              Mapping.csv auswählen (optional)
            </Button>

            <Typography variant="caption" color="text.secondary">
              Nur nötig bei abweichenden Spaltennamen
            </Typography>

            {mappingFile && (
              <Typography variant="caption">Ausgewählt: {mappingFile.name}</Typography>
            )}

            {/* ================================================= */}
            {/* DRY RUN */}
            {/* ================================================= */}

            <FormControlLabel
              control={
                <Switch
                  checked={dryRun}
                  disabled={loading}
                  onChange={(event) => {
                    setDryRun(event.target.checked);

                    // Der bisherige Report gehört zum alten Modus
                    setReport(null);
                  }}
                />
              }
              label="Dry-Run (nur prüfen)"
            />

            {/* ================================================= */}
            {/* REPORT */}
            {/* ================================================= */}

            {report && (
              <Stack spacing={2}>
                <Alert severity={report.errors > 0 ? "warning" : "success"}>
                  {report.created} erstellt, {report.simulated} geprüft, {report.errors} Fehler
                </Alert>

                {report.errorDetails?.length > 0 && (
                  <Stack spacing={1}>
                    <Typography variant="subtitle2">Fehlerdetails</Typography>

                    <TableContainer
                      component={Paper}
                      sx={{
                        maxHeight: 300,
                        border: "1px solid #eee",
                      }}
                    >
                      <Table size="small" stickyHeader>
                        <TableHead>
                          <TableRow>
                            <TableCell>Zeile</TableCell>
                            <TableCell>Feld</TableCell>
                            <TableCell>Wert</TableCell>
                            <TableCell>Fehler</TableCell>
                          </TableRow>
                        </TableHead>

                        <TableBody>
                          {report.errorDetails.map((err, index) => (
                            <TableRow key={index} hover>
                              <TableCell>{err.row}</TableCell>

                              <TableCell>{err.field ?? "-"}</TableCell>

                              <TableCell>
                                <code>{err.value ?? "-"}</code>
                              </TableCell>

                              <TableCell>{err.message}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Stack>
                )}
              </Stack>
            )}
          </Stack>
        </DialogContent>

        {/* ===================================================== */}
        {/* ACTIONS */}
        {/* ===================================================== */}

        <DialogActions>
          <Button onClick={handleClose} disabled={loading}>
            Schließen
          </Button>

          <Button
            variant="contained"
            onClick={() => void handleImport()}
            disabled={loading || !csvFile}
          >
            {loading ? "Bitte warten ..." : dryRun ? "Prüfen" : "Importieren"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* ======================================================= */}
      {/* ERROR DIALOG */}
      {/* ======================================================= */}

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </>
  );
}
