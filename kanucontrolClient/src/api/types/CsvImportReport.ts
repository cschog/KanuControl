export interface CsvImportError {
  row: number;
  field?: string | null;
  value?: string | null;
  message: string;
}

export interface CsvImportReport {
  totalRows: number;
  created: number;
  simulated: number;
  skipped: number;
  errors: number;
  errorDetails: CsvImportError[];
}
