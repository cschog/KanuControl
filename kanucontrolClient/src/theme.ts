import { createTheme } from "@mui/material/styles";
import { radius } from "@/theme/ui";

const theme = createTheme({
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
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
    borderRadius: radius.dialog,
  },

  components: {
    MuiAccordion: {
      styleOverrides: {
        root: {
          "@media (min-width: 1100px)": {
            marginBottom: "12px",

            border: "1px solid",
            borderColor: "divider",
            borderRadius: 8,

            // Macht geschlossene Accordions als eigene Elemente sichtbar
            boxShadow: "0 1px 3px rgba(0, 0, 0, 0.08)",

            overflow: "hidden",

            "&:before": {
              display: "none",
            },

            "&.Mui-expanded": {
              margin: "0 0 12px 0",

              // geöffnet etwas stärker hervorheben
              boxShadow: "0 2px 6px rgba(0, 0, 0, 0.12)",
            },
          },
        },
      },
    },

    MuiAccordionSummary: {
      styleOverrides: {
        root: {
          "@media (min-width: 1100px)": {
            minHeight: 56,
            paddingLeft: 16,
            paddingRight: 16,

            // Leicht grauer Header
            backgroundColor: "#f5f5f5",

            transition: "background-color 150ms ease",

            "&:hover": {
              backgroundColor: "#e8e8e8",
            },

            "&.Mui-expanded": {
              minHeight: 56,

              // geöffnet ebenfalls leicht grau
              backgroundColor: "#cdcdcd",
            },
          },
        },

        content: {
          "@media (min-width: 1100px)": {
            marginTop: 12,
            marginBottom: 12,

            "&.Mui-expanded": {
              marginTop: 12,
              marginBottom: 12,
            },
          },
        },
      },
    },
  },
});

export default theme;
