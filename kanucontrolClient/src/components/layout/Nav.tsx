import React, { useEffect, useState } from "react";
import packageJson from "../../../package.json";
import Popover from "@mui/material/Popover";
import { getOnlineUsers } from "@/api/services/sessionApi";
import { InfoDialog } from "@/components/info/InfoDialog";
import { InfoMenuDialog } from "@/components/info/InfoMenuDialog";

import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  useMediaQuery,
  IconButton,
  CircularProgress,
} from "@mui/material";
import GroupsIcon from "@mui/icons-material/Groups";
import Badge from "@mui/material/Badge";
import { useTheme } from "@mui/material/styles";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline";
import LogoutIcon from "@mui/icons-material/Logout";

import keycloak from "@/auth/keycloak";
import { useNavigate } from "react-router-dom";
import { useAppContext } from "@/context/AppContext";

import { getBackendVersion } from "@/api/services/systemApi";
import apiClient from "@/api/client/apiClient";
import { InfoPage } from "@/api/enums/InfoPage";
import { radius } from "@/theme/ui";

const Navigation = () => {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const { schema, active, loading } = useAppContext();
  const [onlineUsers, setOnlineUsers] = useState<string[]>([]);
  const [onlineAnchor, setOnlineAnchor] = useState<HTMLElement | null>(null);
  const [backendVersion, setBackendVersion] = useState("...");
  const [menuOpen, setMenuOpen] = useState(false);
  const [infoPage, setInfoPage] = useState<InfoPage | null>(null);

  useEffect(() => {
    getBackendVersion()
      .then(setBackendVersion)
      .catch(() => setBackendVersion("n/a"));
  }, []);

  const handleHome = () => navigate("/startmenue");

  const handleOnlineClick = (event: React.MouseEvent<HTMLElement>) => {
    setOnlineAnchor(event.currentTarget);
  };

  const handleOnlineClose = () => {
    setOnlineAnchor(null);
  };

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

const loadOnlineUsers = async () => {
  try {
    const users = await getOnlineUsers();

    setOnlineUsers(users);
  } catch (error) {
    console.error("Fehler beim Laden der Online-Benutzer", error);
  }
};

  useEffect(() => {
    loadOnlineUsers();

    const interval = setInterval(loadOnlineUsers, 60000); // alle 60 Sekunden

    return () => clearInterval(interval);
  }, []);

 const onlineUsersBadge =
   onlineUsers.length > 0 ? (
     <>
       <IconButton color="inherit" size="small" onClick={handleOnlineClick} sx={{ mt: 1 }}>
         <Badge badgeContent={onlineUsers.length} color="secondary">
           <GroupsIcon fontSize="small" />
         </Badge>
       </IconButton>

       <Popover
         open={Boolean(onlineAnchor)}
         anchorEl={onlineAnchor}
         onClose={handleOnlineClose}
         anchorOrigin={{
           vertical: "bottom",
           horizontal: "right",
         }}
         transformOrigin={{
           vertical: "top",
           horizontal: "right",
         }}
       >
         <Box sx={{ p: 2, minWidth: 180 }}>
           <Typography variant="subtitle2" sx={{ mb: 1 }}>
             Online im Verein
           </Typography>

           {onlineUsers.map((user) => (
             <Typography key={user} variant="body2">
               {user}
             </Typography>
           ))}
         </Box>
       </Popover>
     </>
   ) : null;

  return (
    <AppBar position="static" sx={{ bgcolor: "#6B7280" }}>
      <Toolbar sx={{ py: isMobile ? 1 : 0 }}>
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
                borderRadius: radius.dialog,
                transition: "all 0.15s ease",
                "&:hover": {
                  backgroundColor: "rgba(255,255,255,0.06)",
                },
              }}
            >
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
              </Box>
            )}
          </Box>
        </Box>

        {/* ================= ACTIONS ================= */}

        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-end",
            ml: 2,
            flexShrink: 0,
          }}
        >
          {/* Zeile 1 */}
          <Box>
            <IconButton color="inherit" onClick={() => setMenuOpen(true)}>
              <HelpOutlineIcon />
            </IconButton>

            <IconButton color="inherit" onClick={handleLogout}>
              <LogoutIcon />
            </IconButton>
          </Box>

          {/* Zeile 2 */}
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              minHeight: 24,
            }}
          >
            {onlineUsersBadge}
          </Box>
        </Box>
      </Toolbar>

      <InfoMenuDialog
        open={menuOpen}
        onClose={() => setMenuOpen(false)}
        onSelect={(page) => setInfoPage(page)}
      />

      {infoPage && <InfoDialog open page={infoPage} onClose={() => setInfoPage(null)} />}
    </AppBar>
  );
};

export default React.memo(Navigation);
