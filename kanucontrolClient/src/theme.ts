import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,

      // vorher 900
      md: 1100,

      lg: 1400,
      xl: 1920,
    },
  },

  palette: {
    primary: {
      main: "#1976d2",
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
