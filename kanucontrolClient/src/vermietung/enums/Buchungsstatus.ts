// src/vermietung/enums/Buchungsstatus.ts
export type Buchungsstatus =
  | "ANFRAGE"
  | "RESERVIERT"
  | "BESTAETIGT"
  | "ABRECHENBAR"
  | "ABGESCHLOSSEN"
  | "ABGELEHNT"
  | "STORNIERT";

export const BUCHUNGSSTATUS_LABELS: Record<Buchungsstatus, string> = {
  ANFRAGE: "Anfrage",
  RESERVIERT: "Reserviert",
  BESTAETIGT: "Bestätigt",
  ABRECHENBAR: "Abrechenbar",
  ABGESCHLOSSEN: "Abgeschlossen",
  ABGELEHNT: "Abgelehnt",
  STORNIERT: "Storniert",
};
