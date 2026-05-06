import React from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  IconButton,
  CircularProgress,
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import LogoutIcon from "@mui/icons-material/Logout";
import keycloak from "@/auth/keycloak";
import { useNavigate } from "react-router-dom";
import { useAppContext } from "@/context/AppContext";

const Navigation = () => {
  const navigate = useNavigate();
  const { schema, active, loading } = useAppContext();

  const handleHome = () => navigate("/startmenue");
 const handleLogout = () => keycloak.logout({ redirectUri: window.location.origin });

  const contextText = active
    ? `${schema} · ${active.name} · ${
        active.leiter ? `${active.leiter.vorname ?? ""} ${active.leiter.name ?? ""}` : "kein Leiter"
      }`
    : schema;

  return (
    <AppBar position="static" sx={{ bgcolor: "#6B7280" }}>
      <Toolbar>
        {/* 🔰 Logo */}
        <Box
          component="img"
          src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
          alt="KanuControl"
          sx={{ height: 60, mr: 3 }}
        />

        {/* 📌 Titel + Kontext */}
        <Box sx={{ flexGrow: 1, lineHeight: 1.1 }}>
          <Typography
            variant="h5"
            fontWeight={800}
            sx={{
              letterSpacing: 0.5,
            }}
          >
            KanuControl
          </Typography>

          {loading ? (
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mt: 0.5 }}>
              <CircularProgress size={14} color="inherit" />
              <Typography variant="body2" sx={{ opacity: 0.8 }}>
                Lade Kontext…
              </Typography>
            </Box>
          ) : (
            <Typography
              variant="subtitle1"
              fontWeight={700}
              sx={{
                opacity: 0.98,
                fontSize: "1.35rem",
                letterSpacing: 0.2,
                whiteSpace: "normal",
                wordBreak: "break-word",
                lineHeight: 1.2,
              }}
            >
              {contextText}
            </Typography>
          )}
        </Box>

        {/* 🏠 Home */}
        <IconButton color="inherit" onClick={handleHome}>
          <HomeIcon />
        </IconButton>

        {/* 🚪 Logout */}
        <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
};

export default React.memo(Navigation);
