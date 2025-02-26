import { styled } from "@mui/material";

const DropZone = styled("div")(({ theme }) => ({
    border: `2px dashed ${theme.palette.primary.main}`,
    borderRadius: theme.shape.borderRadius,
    padding: theme.spacing(3),
    textAlign: "center",
    color: theme.palette.text.secondary,
    cursor: "pointer",
}));

export { DropZone };
