import React from "react";
import {createRoot} from "react-dom/client";
import {ThemeProvider,createTheme,CssBaseline} from "@mui/material";
import SimulationPage from "../../src/kjfp/components/simulation/SimulationPage";
import keycloak from "../../src/core/auth/keycloak";
keycloak.updateToken=async()=>false;
createRoot(document.getElementById("root")!).render(<ThemeProvider theme={createTheme()}><CssBaseline/><main style={{padding:16}}><SimulationPage veranstaltungId={7}/></main></ThemeProvider>);
