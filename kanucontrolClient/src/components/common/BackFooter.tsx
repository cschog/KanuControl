import { Box, Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";
import { useEffect, useRef } from "react";

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
  onHeightChange?: (height: number) => void;
}

export default function BackFooter({
  label,
  path,
  onClick,
  actions,
  floating = false,
  onHeightChange,
}: Props) {
  const navigate = useNavigate();

  const footerRef = useRef<HTMLDivElement>(null);

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

  useEffect(() => {
    if (!floating || !footerRef.current || !onHeightChange) {
      return;
    }

    const element = footerRef.current;

    const updateHeight = () => {
      onHeightChange(element.getBoundingClientRect().height);
    };

    updateHeight();

    const observer = new ResizeObserver(updateHeight);
    observer.observe(element);

    return () => {
      observer.disconnect();
    };
  }, [floating, onHeightChange]);

  return (
    <Box
      ref={footerRef}
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

          width: {
            xs: "calc(100% - 32px)",
            md: "auto",
          },
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

          width: {
            xs: "100%",
            md: "auto",
          },
        }}
      >
        {allActions.map((action) => (
          <Button
            key={action.label}
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => handleAction(action)}
            sx={{
              width: {
                xs: "100%",
                md: 180,
              },
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
