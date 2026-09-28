import { Box, Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";

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

  const [bottomOffset, setBottomOffset] = useState(8);

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

  /*
   * Höhe des Footers an AppLayout melden
   */
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

  /*
   * Prüfen, ob unten ein BottomActionBar sichtbar ist.
   *
   * Wenn ja, wird der BackFooter automatisch darüber positioniert.
   */
  useEffect(() => {
    if (!floating) {
      setBottomOffset(8);
      return;
    }

    const updateBottomOffset = () => {
      const actionBar = document.querySelector<HTMLElement>("[data-bottom-action-bar]");

      if (!actionBar) {
        setBottomOffset(8);
        return;
      }

      const rect = actionBar.getBoundingClientRect();

      const viewportHeight = window.innerHeight;

      /*
       * Wie weit reicht der BottomActionBar in den unteren
       * Bereich des Viewports hinein?
       */
      const visibleHeight = Math.max(0, viewportHeight - rect.top);

      /*
       * Nur wenn der BottomActionBar tatsächlich unten
       * sichtbar ist, gehen wir darüber.
       */
      if (rect.top < viewportHeight && visibleHeight > 0) {
        setBottomOffset(visibleHeight + 8);
      } else {
        setBottomOffset(8);
      }
    };

    updateBottomOffset();

    window.addEventListener("resize", updateBottomOffset);
    window.addEventListener("scroll", updateBottomOffset, {
      passive: true,
    });

    const observer = new ResizeObserver(updateBottomOffset);

    const actionBar = document.querySelector<HTMLElement>("[data-bottom-action-bar]");

    if (actionBar) {
      observer.observe(actionBar);
    }

    return () => {
      window.removeEventListener("resize", updateBottomOffset);
      window.removeEventListener("scroll", updateBottomOffset);
      observer.disconnect();
    };
  }, [floating]);

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
          bottom: `${bottomOffset}px`,
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
