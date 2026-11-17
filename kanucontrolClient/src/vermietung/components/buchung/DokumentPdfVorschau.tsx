import { useEffect, useState } from "react";
import { Alert, Box, Button, Stack } from "@mui/material";

/** One viewer owns its URL for the entire lifetime of its PDF frame. */
export default function DokumentPdfVorschau({ pdf, dateiname, title, height = 500 }: {
  pdf: string; dateiname: string; title: string; height?: number;
}) {
  const [preview, setPreview] = useState<{ pdf: string; url: string } | null>(null);
  const [error, setError] = useState(false);
  useEffect(() => {
    setError(false);
    try {
      const data = new Blob([Uint8Array.from(atob(pdf), c => c.charCodeAt(0))], { type: "application/pdf" });
      const url = URL.createObjectURL(data);
      setPreview({ pdf, url });
      return () => URL.revokeObjectURL(url);
    } catch {
      setPreview(null); setError(true);
    }
  }, [pdf]);
  const url = preview?.pdf === pdf ? preview.url : "";
  return <Stack spacing={1}>
    {url && <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
      <Button component="a" href={url} target="_blank" rel="noopener noreferrer">PDF separat öffnen</Button>
      <Button component="a" href={url} download={dateiname}>PDF herunterladen</Button>
    </Stack>}
    {error && <Alert severity="warning">Die eingebettete PDF-Vorschau konnte nicht geladen werden. Bitte die PDF separat öffnen oder herunterladen.</Alert>}
    {url && <Box component="iframe" key={url} title={title} src={url} onError={() => setError(true)}
      sx={{ width: "100%", height, minHeight: height, flexShrink: 0, border: 1, borderColor: "divider" }} />}
  </Stack>;
}
