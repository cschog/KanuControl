import React from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider, createTheme, CssBaseline } from "@mui/material";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { VeranstaltungSections } from "../../src/kjfp/components/veranstaltung/form/VeranstaltungSections";
import { cleanProgramHtml, programText, programErrors } from "../../src/kjfp/components/veranstaltung/form/programmentwurfHtml";
import { VeranstaltungFormModel } from "../../src/kjfp/types/veranstaltung/VeranstaltungFormModel";
import { useVeranstaltungForm } from "../../src/kjfp/components/veranstaltung/hook/useVeranstaltungForm";
import keycloak from "../../src/core/auth/keycloak";
keycloak.updateToken = async () => false;
Object.assign(window, { cleanProgramHtml, programText, programErrors });
const initial: VeranstaltungFormModel = { name: "Herbstfreizeit", kjfpAktiv: true, fahrtenAktiv: false, beginnDatum: "2026-11-13", endeDatum: "2026-11-15", beginnZeit: "10:00", endeZeit: "18:00", programmentwurfTitel: null, programmentwurfInhalt: null };
function Preview() {
  const { form, update } = useVeranstaltungForm(initial);
  if (!form) return null;
  return <ThemeProvider theme={createTheme()}><CssBaseline /><LocalizationProvider dateAdapter={AdapterDayjs}><main style={{padding:16, minWidth:0}}><VeranstaltungSections form={form} editMode beitragsstrukturen={[]} onChange={update} /><pre id="state" style={{whiteSpace:"pre-wrap",overflowWrap:"anywhere"}}>{JSON.stringify(form)}</pre></main></LocalizationProvider></ThemeProvider>;
}
createRoot(document.getElementById("root")!).render(<Preview />);
