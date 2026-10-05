import FeedbackIcon from "@mui/icons-material/Feedback";
import { Fab, Menu, MenuItem, ListItemIcon, ListItemText } from "@mui/material";
import LightbulbIcon from "@mui/icons-material/Lightbulb";
import MapIcon from "@mui/icons-material/Map";
import { useState } from "react";

export const FeedbackFab = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const open = Boolean(anchorEl);

  return (
    <>
      <Fab
        color="primary"
        onClick={(e) => setAnchorEl(e.currentTarget)}
        sx={{
          position: "fixed",
          bottom: 24,
          right: 24,
          zIndex: 2000,
        }}
      >
        <FeedbackIcon />
      </Fab>

      <Menu anchorEl={anchorEl} open={open} onClose={() => setAnchorEl(null)}>
        <MenuItem onClick={() => window.open("https://kanucontrol.featurebase.app", "_blank")}>
          <ListItemIcon>
            <LightbulbIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Feedback geben</ListItemText>
        </MenuItem>

        <MenuItem
          onClick={() => window.open("https://kanucontrol.featurebase.app/roadmap", "_blank")}
        >
          <ListItemIcon>
            <MapIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Roadmap</ListItemText>
        </MenuItem>
      </Menu>
    </>
  );
};
