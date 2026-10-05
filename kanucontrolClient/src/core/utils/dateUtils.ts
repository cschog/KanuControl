// src/utils/dateUtils.ts
export function normalizeGermanDate(input: string): string | null {
  if (!input) return null;

  const value = input.trim();

  // ISO schon korrekt
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return value;
  }

  // DD.MM.YYYY oder D.M.YYYY
  let m = value.match(/^(\d{1,2})\.(\d{1,2})\.(\d{4})$/);
  if (m) {
    const [, d, mo, y] = m;
    return buildIsoDate(d, mo, y);
  }

  // DD.MM.YY
  m = value.match(/^(\d{1,2})\.(\d{1,2})\.(\d{2})$/);
  if (m) {
    const [, d, mo, y] = m;
    const year = Number(y) < 50 ? `20${y}` : `19${y}`;
    return buildIsoDate(d, mo, year);
  }

  return null;
}

export function buildIsoDate(d: string, m: string, y: string): string | null {
  const day = Number(d);
  const month = Number(m);
  const year = Number(y);

  if (month < 1 || month > 12) return null;
  if (day < 1 || day > 31) return null;
  if (year < 1 || year > 9999) return null;

  const date = new Date(year, month - 1, day);

  // echte Validierung (kein 31.02.)
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return null;
  }

  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

export function formatGermanDate(date: string | null | undefined): string {
  if (!date) {
    return "";
  }

  // ISO-Format
  if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
    const [year, month, day] = date.split("-");
    return `${day}.${month}.${year}`;
  }

  // Bereits deutsches Format
  if (/^\d{1,2}\.\d{1,2}\.\d{4}$/.test(date)) {
    return date;
  }

  // Fallback
  const d = new Date(date);

  if (isNaN(d.getTime())) {
    return date;
  }

  return d.toLocaleDateString("de-DE");
}
