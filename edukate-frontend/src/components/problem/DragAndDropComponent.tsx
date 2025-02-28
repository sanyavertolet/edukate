import { CloudUpload } from "@mui/icons-material";
import { Typography } from "@mui/material";
import { DropZone } from "../Styled.ts";

interface DragAndDropComponentProps {
    hidden: boolean;
}

export default function DragAndDropComponent({ hidden }: DragAndDropComponentProps) {
    return (
        <DropZone hidden={ hidden }>
            <CloudUpload sx={{ fontSize: 48, mb: 1 }} />
            <Typography variant="body1">Drag and drop your solution files here (images or PDF)</Typography>
            <Typography variant="caption">(Upload functionality is currently disabled)</Typography>
        </DropZone>
    );
}
