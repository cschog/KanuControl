import { Box, Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";

interface Action {
  label: string;
  path?: string;
  onClick?: () => void;
}

interface Props {
  label?: string;
  path?: string;
  onClick?: () => void;
  actions?: Action[];
  floating?: boolean;
}

export default function BackFooter({ label, path, onClick, actions, floating = false }: Props) {
  const navigate = useNavigate();

  const allActions: Action[] =
    actions ??
    (label
      ? [
          {
            label,
            path,
            onClick,
          },
        ]
      : []);

  const handleAction = (action: Action) => {
    if (action.onClick) {
      action.onClick();
    } else if (action.path) {
      navigate(action.path);
    }
  };

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        mt: 2,
        mb: 0.5,

        ...(floating && {
          position: "fixed",
          bottom: 8,
          left: "50%",
          transform: "translateX(-50%)",
          zIndex: 1100,
        }),
      }}
    >
      <Box
        sx={{
          display: "flex",
          flexDirection: { xs: "column", md: "row" },
          gap: 0.75,
          px: 0.5,
          py: 0.5,
          bgcolor: "background.paper",
          borderRadius: 2,
          boxShadow: 3,
        }}
      >
        {allActions.map((action) => (
          <Button
            key={action.label}
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => handleAction(action)}
            sx={{
              width: 180,
              minHeight: 36,
            }}
          >
            {action.label}
          </Button>
        ))}
      </Box>
    </Box>
  );
}
