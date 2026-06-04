import React, { useEffect, useState } from "react";
import packageJson from "../../../package.json";

import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  useMediaQuery,
  IconButton,
  CircularProgress,
} from "@mui/material";
import { useTheme } from "@mui/material/styles";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline";
import LogoutIcon from "@mui/icons-material/Logout";

import keycloak from "@/auth/keycloak";
import { useNavigate } from "react-router-dom";
import { useAppContext } from "@/context/AppContext";

import { getBackendVersion } from "@/api/services/systemApi";
import apiClient from "@/api/client/apiClient";

const Navigation = () => {
  const navigate = useNavigate();

  const theme = useTheme();

  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const { schema, active, loading } = useAppContext();

  const [backendVersion, setBackendVersion] = useState("...");

  useEffect(() => {
    getBackendVersion()
      .then(setBackendVersion)
      .catch(() => setBackendVersion("n/a"));
  }, []);

  const handleHome = () => navigate("/startmenue");

  const handleLogout = async () => {
    try {
      await apiClient.post("/admin/audit/logout");
    } catch (error) {
      console.error("Audit-Logout konnte nicht gespeichert werden", error);
    }

    await keycloak.logout({
      redirectUri: window.location.origin,
    });
  };

  // const contextText = active
  //   ? `${schema} · ${active.name} · ${
  //       active.leiter ? `${active.leiter.vorname ?? ""} ${active.leiter.name ?? ""}` : "kein Leiter"
  //     }`
  //   : schema;

  return (
    <AppBar position="static" sx={{ bgcolor: "#6B7280" }}>
      <Toolbar
        sx={{
          alignItems: isMobile ? "flex-start" : "center",

          flexDirection: isMobile ? "column" : "row",

          gap: isMobile ? 1 : 0,

          py: isMobile ? 1 : 0,
        }}
      >
        {/* ================= MAIN CONTENT ================= */}

        <Box
          sx={{
            flexGrow: 1,
            width: "100%",

            display: "flex",
            flexDirection: "column",
            justifyContent: "center",

            minHeight: isMobile ? "auto" : 60,
          }}
        >
          {/* ================= TOP ROW ================= */}

          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              minHeight: isMobile ? "auto" : 72,
            }}
          >
            {/* LEFT / HOME AREA */}
            <Box
              onClick={handleHome}
              sx={{
                display: "flex",
                alignItems: "center",
                minWidth: 0,
                flexGrow: 1,
                cursor: "pointer",
                borderRadius: 1,
                transition: "all 0.15s ease",
                "&:hover": {
                  backgroundColor: "rgba(255,255,255,0.06)",
                },
              }}
            >
              {/* 🔰 Logo */}
              <Box
                component="img"
                src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
                alt="KanuControl"
                sx={{
                  height: isMobile ? 42 : 60,
                  mr: 2,
                  flexShrink: 0,
                }}
              />

              {/* TITLE */}
              <Box sx={{ minWidth: 0 }}>
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "baseline",
                    gap: isMobile ? 0.5 : 1,
                    flexWrap: isMobile ? "nowrap" : "wrap",
                  }}
                >
                  <Typography variant={isMobile ? "h6" : "h5"} fontWeight={500} noWrap>
                    KanuControl
                  </Typography>

                  {!isMobile && (
                    <Typography
                      variant="caption"
                      sx={{
                        opacity: 0.7,
                        fontSize: "0.7rem",
                      }}
                    >
                      FE {packageJson.version}
                      {" • "}
                      BE {backendVersion}
                    </Typography>
                  )}
                </Box>
              </Box>
            </Box>

            {/* MOBILE ACTIONS */}
            {isMobile && (
              <Box
                sx={{
                  display: "flex",
                  flexShrink: 0,
                }}
              >
                <IconButton
                  color="inherit"
                  onClick={() => {
                    // später Hilfe öffnen
                    alert("Hilfe folgt");
                  }}
                >
                  <HelpOutlineIcon />
                </IconButton>

                <IconButton color="inherit" onClick={handleLogout}>
                  <LogoutIcon />
                </IconButton>
              </Box>
            )}
          </Box>

          {/* ================= CONTEXT ================= */}

          <Box
            sx={{
              mt: isMobile ? 0.5 : 0,
              ml: isMobile ? 0 : "74px",
            }}
          >
            {loading ? (
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  gap: 1,
                }}
              >
                <CircularProgress size={14} color="inherit" />

                <Typography
                  variant="body2"
                  sx={{
                    opacity: 0.8,
                  }}
                >
                  Lade Kontext…
                </Typography>
              </Box>
            ) : (
              <Box>
                <Typography
                  variant="subtitle1"
                  fontWeight={400}
                  sx={{
                    opacity: 0.98,
                    fontSize: isMobile ? "0.9rem" : "1.2rem",
                    letterSpacing: 0.2,
                    lineHeight: 1.3,
                  }}
                >
                  {active
                    ? isMobile
                      ? `${active.name}${
                          active.leiter ? ` · ${active.leiter.vorname} ${active.leiter.name}` : ""
                        }`
                      : `${schema} · ${active.name} · ${
                          active.leiter
                            ? `${active.leiter.vorname} ${active.leiter.name}`
                            : "kein Leiter"
                        }`
                    : schema}
                </Typography>

                {!isMobile && active?.leiter && (
                  <Typography
                    variant="subtitle1"
                    fontWeight={400}
                    sx={{
                      opacity: 0.98,
                      fontSize: "1.2rem",
                      letterSpacing: 0.2,
                      lineHeight: 1.3,
                    }}
                  ></Typography>
                )}
              </Box>
            )}
          </Box>
        </Box>

        {/* ================= DESKTOP ACTIONS ================= */}

        {!isMobile && (
          <Box
            sx={{
              display: "flex",
              ml: 2,
              flexShrink: 0,
            }}
          >
            <IconButton
              color="inherit"
              onClick={() => {
                // später Hilfe öffnen
                alert("Hilfe folgt");
              }}
            >
              <HelpOutlineIcon />
            </IconButton>

            <IconButton color="inherit" onClick={handleLogout}>
              <LogoutIcon />
            </IconButton>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default React.memo(Navigation);
