import { ThemeProvider, CssBaseline } from "@mui/material";
import theme from "@/theme";
import { useAuth } from "@/auth/useAuth";
import Public from "@/pages/Public";
import App from "@/App";

export default function Root() {
  const { initialized, authenticated } = useAuth();

  if (!initialized) {
    return <div style={{ padding: 24 }}>Lade …</div>;
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {authenticated ? <App /> : <Public />}
    </ThemeProvider>
  );
}
