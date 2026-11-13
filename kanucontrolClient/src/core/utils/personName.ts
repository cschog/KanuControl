/** Platzhalter bleiben in den Stammdaten erhalten, werden aber nicht als Vorname angezeigt. */
export function displayVorname(vorname?: string | null): string {
  const text = vorname?.trim() ?? "";
  return /^_+$/.test(text) ? "" : text;
}

export function formatPersonName(name?: string | null, vorname?: string | null,
  order: "last" | "first" = "last"): string {
  const nachname = name?.trim() ?? "";
  const first = displayVorname(vorname);
  if (!first) return nachname;
  if (!nachname) return first;
  return order === "first" ? `${first} ${nachname}` : `${nachname}, ${first}`;
}
