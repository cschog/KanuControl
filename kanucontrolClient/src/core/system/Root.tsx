import { ThemeProvider, CssBaseline } from "@mui/material";
import theme from "@/core/theme/theme";
import { useAuth } from "@/core/auth/useAuth";
import Public from "@/core/system/Public";
import App from "@/core/system/App";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import "dayjs/locale/de";

export default function Root() {
  const { initialized, authenticated } = useAuth();

  if (!initialized) {
    return <div style={{ padding: 24 }}>Lade …</div>;
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="de">
        {authenticated ? <App /> : <Public />}
      </LocalizationProvider>
    </ThemeProvider>
  );
}
