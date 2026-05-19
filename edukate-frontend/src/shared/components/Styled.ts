import {
    Card as MuiCard,
    Drawer as MuiDrawer,
    List as MuiList,
    Paper as MuiPaper,
    Stack,
    styled,
    SwipeableDrawer as MuiSwipeableDrawer,
    Theme,
} from "@mui/material";
import Toolbar from "@mui/material/Toolbar";
import { alpha, CSSObject } from "@mui/material/styles";

export function frostedGlass(theme: Theme, opacity = 0.5): CSSObject {
    return {
        backdropFilter: "blur(6px)",
        backgroundColor: alpha(theme.palette.background.default, 0.65),
        ...theme.applyStyles("dark", {
            backgroundColor: alpha(theme.palette.background.default, opacity),
        }),
    };
}

export const BlurryToolbar = styled(Toolbar)(({ theme }) => ({
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    flexShrink: 0,
    borderRadius: `calc(${String(theme.shape.borderRadius)}px + 8px)`,
    ...frostedGlass(theme),
    border: "1px solid",
    borderColor: theme.palette.divider,
    boxShadow: theme.shadows[1],
    padding: theme.spacing(1, 1.5),
}));

export const Card = styled(MuiCard)(({ theme }) => ({
    ...frostedGlass(theme),
    backgroundColor: alpha(theme.palette.background.paper, 0.65),
    border: "1px solid",
    borderColor: theme.palette.divider,
    borderRadius: theme.shape.borderRadius * 3,
    boxShadow: "none",
}));

export const Paper = styled(MuiPaper)(({ theme }) => ({
    ...frostedGlass(theme),
    backgroundColor: alpha(theme.palette.background.paper, 0.65),
    border: "1px solid",
    borderColor: theme.palette.divider,
    borderRadius: theme.shape.borderRadius * 3,
    boxShadow: "none",
}));

export const List = styled(MuiList)(({ theme }) => ({
    ...frostedGlass(theme),
    backgroundColor: alpha(theme.palette.background.paper, 0.65),
    borderRadius: theme.shape.borderRadius * 3,
    border: "1px solid",
    borderColor: theme.palette.divider,
}));

export const Drawer = styled(MuiDrawer)(({ theme }) => ({
    "& .MuiPaper-root": {
        ...frostedGlass(theme),
        backgroundColor: alpha(theme.palette.background.paper, 0.65),
    },
}));

export const SwipeableDrawer = styled(MuiSwipeableDrawer)(({ theme }) => ({
    "& .MuiPaper-root": {
        ...frostedGlass(theme),
        backgroundColor: alpha(theme.palette.background.paper, 0.65),
    },
}));

export const SignCard = styled(MuiCard)(({ theme }) => ({
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
