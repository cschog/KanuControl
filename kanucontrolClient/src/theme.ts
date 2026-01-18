import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: {
      main: "#1976d2", // MUI default blue
    },
    secondary: {
      main: "#9c27b0",
    },
  },
  shape: {
    borderRadius: 8,
  },
});

export default theme;