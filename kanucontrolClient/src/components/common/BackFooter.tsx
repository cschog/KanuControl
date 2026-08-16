import { Box, Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";

interface Props {
  label: string;
  path: string;
}

export default function BackFooter({ label, path }: Props) {
  const navigate = useNavigate();

  return (
    <>
      {/* Platz für den schwebenden Footer */}
      <Box sx={{ height: 80 }} />

      {/* schwebender Footer */}
      <Box
        sx={{
          position: "fixed",
          bottom: 16,
          left: "50%",
          transform: "translateX(-50%)",
          zIndex: 1100,

          px: 1,
          py: 1,

          bgcolor: "background.paper",
          borderRadius: 2,
          boxShadow: 3,

          border: 1,
          borderColor: "divider",
        }}
      >
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(path)}>
          {label}
        </Button>
      </Box>
    </>
  );
}
