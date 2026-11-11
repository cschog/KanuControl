import { Box, Button } from "@mui/material";
import { alpha } from "@mui/material/styles";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import HomeOutlinedIcon from "@mui/icons-material/HomeOutlined";
import { useNavigate } from "react-router-dom";

interface Action {
  label: string;
  path?: string;
  onClick?: () => void;
  back?: boolean;
}

interface Props {
  label?: string;
  path?: string;
  onClick?: () => void;
  actions?: Action[];
}

export default function BackFooter({ label, path, onClick, actions }: Props) {
  const navigate = useNavigate();
  const allActions: Action[] = actions ?? (label ? [{ label, path, onClick }] : []);

  const handleAction = (action: Action) => {
    if (action.onClick) {
      action.onClick();
    } else if (action.back) {
      navigate(-1);
    } else if (action.path) {
      navigate(action.path);
    }
  };

  if (allActions.length === 0) return null;

  return (
    <Box
      component="nav"
      aria-label="Seitennavigation"
      sx={{
        flexShrink: 0,
        borderTop: 1,
        borderColor: "divider",
        bgcolor: "background.paper",
        boxShadow: "0 -4px 20px rgba(15, 23, 42, 0.04)",
        px: { xs: 2, md: 4 },
        pt: 1,
        pb: "calc(8px + env(safe-area-inset-bottom, 0px))",
      }}
    >
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 1,
          width: "100%",
          maxWidth: 1440,
          mx: "auto",
        }}
      >
        {allActions.map((action) => {
          const isModuleLink = !!action.path && !action.back;
          return (
            <Button
              key={action.label}
              variant={isModuleLink ? "outlined" : "text"}
              startIcon={isModuleLink ? <HomeOutlinedIcon /> : <ArrowBackIcon />}
              onClick={() => handleAction(action)}
              sx={(theme) => ({
                minHeight: 44,
                minWidth: 0,
                maxWidth: "100%",
                px: { xs: 1.5, sm: 2 },
                py: 1,
                borderRadius: 2,
                textTransform: "none",
                fontWeight: 600,
                lineHeight: 1.4,
                ...(isModuleLink && {
                  ml: "auto",
                  borderColor: alpha(theme.palette.primary.main, 0.2),
                  bgcolor: alpha(theme.palette.primary.main, 0.04),
                  "&:hover": {
                    borderColor: alpha(theme.palette.primary.main, 0.45),
                    bgcolor: alpha(theme.palette.primary.main, 0.09),
                  },
                }),
              })}
            >
              {action.label}
            </Button>
          );
        })}
      </Box>
    </Box>
  );
}
