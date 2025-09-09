import { Avatar, Box, IconButton } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { Link as RouterLink } from "react-router-dom";
import { useState } from "react";
import { MobileDrawerComponent } from "./MobileDrawerComponent";
import { useDeviceContext } from "./DeviceContextProvider";

export const ToolbarHomeComponent = () => {
  const { isMobile } = useDeviceContext();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
      <IconButton
        component={RouterLink}
        to="/"
        aria-label="Go home"
        size="small"
      >
        <Avatar alt="Home" src="/logo.png" sx={{ width: 32, height: 32 }} />
      </IconButton>

      {isMobile && (
        <>
          <IconButton
            aria-label="Open menu"
            onClick={() => setIsOpen(true)}
            size="small"
          >
            <MenuIcon />
          </IconButton>
          <MobileDrawerComponent isOpen={isOpen} setIsOpen={setIsOpen} />
        </>
      )}
    </Box>
  );
};
