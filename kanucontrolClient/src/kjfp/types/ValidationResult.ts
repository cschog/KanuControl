// src/api/types/ValidationResult.ts

export interface ValidationMessage {
  severity: "ERROR" | "WARNING";
  message: string;
  field: string | null;
  editableInPdf: boolean;
}

export interface ValidationResult {
  valid: boolean;
  errors: ValidationMessage[];
  warnings: ValidationMessage[];
}
