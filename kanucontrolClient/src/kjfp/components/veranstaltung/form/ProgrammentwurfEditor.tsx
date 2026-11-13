import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, TextField, Typography } from "@mui/material";
import { cleanProgramHtml, hasProgramText, programErrors, programText } from "./programmentwurfHtml";

interface Props { title: string; html: string; editMode: boolean; onChange: (title: string, html: string) => void }
export function ProgrammentwurfEditor({ title, html, editMode, onChange }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [active, setActive] = useState<Record<string, boolean>>({});
  const savedSelection = useRef<Range | null>(null);
  const defaultSuggested = useRef(false);
  useEffect(() => {
    if (ref.current && cleanProgramHtml(ref.current.innerHTML) !== cleanProgramHtml(html)) ref.current.innerHTML = cleanProgramHtml(html);
  }, [html, editMode]);
  useEffect(() => {
    const selection = () => {
      const s = window.getSelection();
      if (!s?.rangeCount || !ref.current?.contains(s.anchorNode)) return;
      savedSelection.current = s.getRangeAt(0).cloneRange();
      setActive(Object.fromEntries(["bold", "italic", "insertUnorderedList", "insertOrderedList"].map(cmd => [cmd, document.queryCommandState(cmd)])));
    };
    document.addEventListener("selectionchange", selection);
    return () => document.removeEventListener("selectionchange", selection);
  }, []);
  const changed = () => {
    const value = cleanProgramHtml(ref.current?.innerHTML ?? "");
    let nextTitle = title;
    if (!defaultSuggested.current && !title && !hasProgramText(html) && hasProgramText(value)) { nextTitle = "Programmentwurf"; defaultSuggested.current = true; }
    onChange(nextTitle, value);
  };
  const command = (cmd: string) => {
    ref.current?.focus();
    if (savedSelection.current) { const s = window.getSelection(); s?.removeAllRanges(); s?.addRange(savedSelection.current); }
    document.execCommand("styleWithCSS", false, "false");
    if (cmd === "removeFormat") {
      // Browserbefehle erhalten Auswahl und Undo-Historie.
      const selection = window.getSelection();
      const range = selection?.rangeCount ? selection.getRangeAt(0) : null;
      if (range && Array.from(ref.current?.querySelectorAll("li") ?? []).some(li => range.intersectsNode(li))) {
        if (!document.queryCommandState("insertUnorderedList")) document.execCommand("insertUnorderedList");
        document.execCommand("insertUnorderedList");
      }
      document.execCommand("removeFormat");
    } else document.execCommand(cmd);
    changed();
  };
  const errors = programErrors(title, html);
  return <Box sx={{ minWidth: 0 }}>
    <TextField label="Überschrift" value={title} onChange={e => onChange(e.target.value, html)} disabled={!editMode} fullWidth error={Array.from(title).length > 200} helperText={`${Array.from(title).length} / 200 Zeichen`} sx={{ mb: 2 }} />
    {editMode && <Box role="toolbar" aria-label="Programmtext formatieren" sx={{ display: "flex", flexWrap: "wrap", gap: 1, mb: 1 }}>
      {[["bold", "Fett (Strg+B)"], ["italic", "Kursiv (Strg+I)"], ["insertUnorderedList", "Aufzählung"], ["insertOrderedList", "Nummerierte Liste"], ["removeFormat", "Formatierung entfernen"]].map(([cmd, label]) =>
        <Button key={cmd} size="small" variant={active[cmd] ? "contained" : "outlined"} aria-pressed={!!active[cmd]} onMouseDown={e => e.preventDefault()} onClick={() => command(cmd)}>{label}</Button>)}
    </Box>}
    <Box ref={ref} role="textbox" aria-label="Programmtext" aria-multiline="true" aria-readonly={!editMode} contentEditable={editMode} suppressContentEditableWarning tabIndex={0}
      onInput={changed}
      onKeyDown={e => {
        if ((e.ctrlKey || e.metaKey) && ["b", "i"].includes(e.key.toLowerCase())) {
          e.preventDefault(); command(e.key.toLowerCase() === "b" ? "bold" : "italic");
        }
      }}
      onPaste={e => {
        if (!editMode) return;
        e.preventDefault();
        const markup = e.clipboardData.getData("text/html");
        const plain = document.createElement("div"); plain.textContent = e.clipboardData.getData("text/plain");
        document.execCommand("insertHTML", false, cleanProgramHtml(markup || plain.innerHTML.replace(/\r?\n/g, "<br>"))); changed();
      }}
      onDrop={e => e.preventDefault()}
      sx={{ border: "1px solid", borderColor: errors.length ? "error.main" : "divider", borderRadius: 1, p: 2, minHeight: 180, maxWidth: "100%", overflowWrap: "anywhere", fontFamily: "inherit", fontSize: 16, "& p": { my: 1 }, "&:focus": { outline: "2px solid", outlineColor: "primary.main" } }} />
    <Typography variant="caption">{Array.from(programText(html)).length} / 50.000 sichtbare Zeichen</Typography>
    {errors.map(error => <Alert severity="error" key={error}>{error}</Alert>)}
  </Box>;
}
