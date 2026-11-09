export const zahl = (n: number | null | undefined, digits = 3) => n == null ? "–" : n.toLocaleString("de-DE", { maximumFractionDigits: digits });
export const datum = (s: string) => new Intl.DateTimeFormat("de-DE", { dateStyle: "short", timeStyle: "short" }).format(new Date(s));
export const monat = (m: number) => new Intl.DateTimeFormat("de-DE", { month: "short" }).format(new Date(2026, m - 1, 1));
