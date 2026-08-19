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
            boxShadow: "none",

            "&:before": {
              display: "none",
            },

            "&.Mui-expanded": {
              margin: "0 0 12px 0",
              borderColor: "divider",
              boxShadow: 1,
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
            backgroundColor: "action.hover",

            "&:hover": {
              backgroundColor: "action.selected",
            },

            "&.Mui-expanded": {
              minHeight: 56,
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
