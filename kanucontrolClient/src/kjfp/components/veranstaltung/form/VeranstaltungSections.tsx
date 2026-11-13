import { useState } from "react";
import { Accordion, AccordionDetails, AccordionSummary, Box, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { VeranstaltungBaseForm } from "./VeranstaltungBaseForm";
import { ProgrammentwurfEditor } from "./ProgrammentwurfEditor";
import { hasProgramText } from "./programmentwurfHtml";
import { VeranstaltungFormModel } from "@/kjfp/types/veranstaltung/VeranstaltungFormModel";

interface Props {
  form: VeranstaltungFormModel; editMode: boolean; detailMode?: boolean;
  beitragsstrukturen: { id: number; name: string }[];
  onChange: <K extends keyof VeranstaltungFormModel>(key: K, value: VeranstaltungFormModel[K]) => void;
  errorAttempt?: number;
}
export function VeranstaltungSections({ form, editMode, detailMode, beitragsstrukturen, onChange, errorAttempt = 0 }: Props) {
  const [dataOpen, setDataOpen] = useState(true);
  const [programOpen, setProgramOpen] = useState(false);
  // Ein fehlgeschlagener Speicherversuch öffnet beide Bereiche; kein Unmount beim Zuklappen.
  const [lastAttempt, setLastAttempt] = useState(errorAttempt);
  if (lastAttempt !== errorAttempt) { setLastAttempt(errorAttempt); setDataOpen(true); setProgramOpen(true); }
  return <Box sx={{ minWidth: 0, mt: 2 }}>
    <Accordion expanded={dataOpen} onChange={(_, open) => setDataOpen(open)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}><Typography>Veranstaltungsdaten</Typography></AccordionSummary>
      <AccordionDetails><Box display="grid" gridTemplateColumns={{ xs: "minmax(0,1fr)", sm: "repeat(2,minmax(0,1fr))", lg: "repeat(4,minmax(0,1fr))" }} gap={2} sx={{ "& > *": { minWidth: 0 } }}>
        <VeranstaltungBaseForm form={form} editMode={editMode} detailMode={detailMode} beitragsstrukturen={beitragsstrukturen} onChange={onChange} />
      </Box></AccordionDetails>
    </Accordion>
    <Accordion expanded={programOpen} onChange={(_, open) => setProgramOpen(open)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}><Box><Typography>Programmentwurf</Typography><Typography variant="caption">{hasProgramText(form.programmentwurfInhalt ?? "") ? "Programmentwurf vorhanden" : "Noch kein Programmentwurf"}</Typography></Box></AccordionSummary>
      <AccordionDetails><ProgrammentwurfEditor title={form.programmentwurfTitel ?? ""} html={form.programmentwurfInhalt ?? ""} editMode={editMode} onChange={(title, html) => { onChange("programmentwurfTitel", title); onChange("programmentwurfInhalt", html); }} /></AccordionDetails>
    </Accordion>
  </Box>;
}
