import React from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider, createTheme, CssBaseline } from "@mui/material";
import BelegDokumentPanel from "../../src/kjfp/components/finanzen/abrechnung/BelegDokumentPanel";
import keycloak from "../../src/core/auth/keycloak";
keycloak.updateToken = async () => false;
createRoot(document.getElementById("root")!).render(<ThemeProvider theme={createTheme()}><CssBaseline/><main style={{ padding: 16 }}><BelegDokumentPanel belegId={4} readOnly={location.search.includes("readonly")}/></main></ThemeProvider>);
