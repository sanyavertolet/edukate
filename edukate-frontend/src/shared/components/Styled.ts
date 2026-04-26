import { Card, Stack, styled } from "@mui/material";
import Toolbar from "@mui/material/Toolbar";
import { alpha } from "@mui/material/styles";

export const BlurryToolbar = styled(Toolbar)(({ theme }) => ({
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    flexShrink: 0,
    borderRadius: `calc(${String(theme.shape.borderRadius)}px + 8px)`,
    backdropFilter: "blur(24px)",
    border: "1px solid",
    borderColor: theme.palette.divider,
    backgroundColor: alpha(theme.palette.background.default, 0.4),
    boxShadow: theme.shadows[1],
    padding: theme.spacing(1, 1.5),
}));

export const SignCard = styled(Card)(({ theme }) => ({
    display: "flex",
    flexDirection: "column",
    alignSelf: "center",
    width: "100%",
    padding: theme.spacing(4),
    gap: theme.spacing(2),
    margin: "auto",
    [theme.breakpoints.up("sm")]: {
        maxWidth: "450px",
    },
    boxShadow: theme.shadows[2],
    ...theme.applyStyles("dark", {
        boxShadow: theme.shadows[4],
    }),
}));

export const SignContainer = styled(Stack)(({ theme }) => ({
    alignItems: "center",
    padding: theme.spacing(2),
    [theme.breakpoints.up("sm")]: {
        padding: theme.spacing(4),
    },
}));
