import { Box } from "@mui/material";
import Navigation from "@/components/layout/Nav";
import { Outlet } from "react-router-dom";

export default function AppLayout() {
  return (
    <>
      <Navigation />
      <Box sx={{ p: { xs: 2, md: 4 } }}>
        <Outlet />
      </Box>
    </>
  );
}