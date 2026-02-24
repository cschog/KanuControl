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

interface Props {
  open: boolean;
  vereinId: number;
  onClose: () => void;
}

export function VereinCsvImportDialog({ open, vereinId, onClose }: Props) {
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [mappingFile, setMappingFile] = useState<File | null>(null);
  const [dryRun, setDryRun] = useState(true);
  const [report, setReport] = useState<CsvImportReport | null>(null);
  const [loading, setLoading] = useState(false);

  const csvInputRef = useRef<HTMLInputElement>(null);
  const mappingInputRef = useRef<HTMLInputElement>(null);

  const downloadMappingTemplate = () => {
    window.open("/api/csv-import/mapping-template", "_blank");
  };

  const handleImport = async () => {
    if (!csvFile) return;

    const form = new FormData();
    form.append("csv", csvFile);

    if (mappingFile) {
      form.append("mapping", mappingFile);
    }

    form.append("dryRun", String(dryRun));

    setLoading(true);
    try {
      const { data } = await apiClient.post<CsvImportReport>(
        `/csv-import/verein/${vereinId}`,
        form,
        { headers: { "Content-Type": "multipart/form-data" } },
      );
      setReport(data);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>CSV-Import Mitglieder</DialogTitle>

      <DialogContent>
        <Stack spacing={2}>
          {/* Mapping Template Download */}
          <Button variant="outlined" onClick={downloadMappingTemplate}>
            Mapping-Template herunterladen
          </Button>

          {/* CSV Datei */}
          <input
            ref={csvInputRef}
            type="file"
            accept=".csv"
            hidden
            onChange={(e) => setCsvFile(e.target.files?.[0] ?? null)}
          />

          <Button
            variant="outlined"
            color={csvFile ? "success" : "primary"}
            onClick={() => csvInputRef.current?.click()}
          >
            CSV-Datei auswählen
          </Button>

          {csvFile && <Typography variant="caption">Ausgewählt: {csvFile.name}</Typography>}

          {/* Mapping Datei (optional) */}
          <input
            ref={mappingInputRef}
            type="file"
            accept=".csv"
            hidden
            onChange={(e) => setMappingFile(e.target.files?.[0] ?? null)}
          />

          <Button
            variant="outlined"
            color={mappingFile ? "success" : "primary"}
            onClick={() => mappingInputRef.current?.click()}
          >
            Mapping.csv auswählen (optional)
          </Button>

          <Typography variant="caption" color="text.secondary">
            Nur nötig bei abweichenden Spaltennamen
          </Typography>

          {mappingFile && <Typography variant="caption">Ausgewählt: {mappingFile.name}</Typography>}

          {/* Dry Run */}
          <FormControlLabel
            control={<Switch checked={dryRun} onChange={(e) => setDryRun(e.target.checked)} />}
            label="Dry-Run (nur prüfen)"
          />

          {/* Report */}
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
                    sx={{ maxHeight: 300, border: "1px solid #eee" }}
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
                        {report.errorDetails.map((err, i) => (
                          <TableRow key={i} hover>
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
        </Stack>{" "}
        {/* ← dieser gehört zum DialogContent */}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Schließen</Button>
        <Button variant="contained" onClick={handleImport} disabled={loading || !csvFile}>
          {dryRun ? "Prüfen" : "Importieren"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
