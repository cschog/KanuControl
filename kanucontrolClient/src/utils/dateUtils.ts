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
    const year = Number(y) < 30 ? `20${y}` : `19${y}`;
    return buildIsoDate(d, mo, year);
  }

  return null;
}

function buildIsoDate(d: string, m: string, y: string): string | null {
  const day = Number(d);
  const month = Number(m);
  const year = Number(y);

  const date = new Date(year, month - 1, day);

  // echte Validierung (kein 31.02.)
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return null;
  }

  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}
