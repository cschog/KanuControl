import React from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  TextField,
  IconButton,
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import LogoutIcon from "@mui/icons-material/Logout";
import keycloak from "@/auth/keycloak";
import { useNavigate } from "react-router-dom";

const Navigation = () => {
  const navigate = useNavigate();

  const handleHome = () => {
    navigate("/startmenue");
  };

  const handleLogout = () => {
    keycloak.logout({ redirectUri: "http://localhost:5173" });
  };

  return (
    <AppBar position="static">
      <Toolbar>
        {/* 🔰 Logo */}
        <Box
          component="img"
          src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
          alt="KanuControl"
          sx={{ height: 30, mr: 2 }}
        />

        {/* 🧭 Titel */}
        <Typography
          variant="h6"
          component="div"
          sx={{ flexGrow: 1 }}
        >
          KanuControl
        </Typography>

        {/* 🔍 Search */}
        <TextField
          size="small"
          placeholder="Search"
          variant="outlined"
          sx={{
            mr: 2,
            bgcolor: "white",
            borderRadius: 1,
          }}
        />

        {/* 🏠 Home */}
        <IconButton color="inherit" onClick={handleHome}>
          <HomeIcon />
        </IconButton>

        {/* 🚪 Logout */}
        <Button
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={handleLogout}
        >
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
};

export default React.memo(Navigation);