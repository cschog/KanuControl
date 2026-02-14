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
  const handleLogout = () => keycloak.logout({ redirectUri: "http://localhost:5173" });

  const contextText = active
    ? `${schema} · ${active.name} · ${
        active.leiter ? `${active.leiter.vorname ?? ""} ${active.leiter.name ?? ""}` : "kein Leiter"
      }`
    : schema;

  return (
    <AppBar position="static">
      <Toolbar>
        {/* 🔰 Logo */}
        <Box
          component="img"
          src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
          alt="KanuControl"
          sx={{ height: 32, mr: 2 }}
        />

        {/* 📌 Titel + Kontext */}
        <Box sx={{ flexGrow: 1, lineHeight: 1.1 }}>
          <Typography variant="h6" fontWeight={700} sx={{ letterSpacing: 0.3 }}>
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
                opacity: 0.95,
                fontSize: "0.95rem",
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
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
