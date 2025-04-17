import { Tooltip } from "@mui/material";
import ErrorIcon from "@mui/icons-material/Error";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile";
import { ExtendedFile } from "./ExtendedFile";

interface ExtendedFileStatusIconProps {
    extendedFile: ExtendedFile;
}

export function ExtendedFileStatusIcon({extendedFile} : ExtendedFileStatusIconProps) {
    return extendedFile.status === 'error'
        ? (<Tooltip title={extendedFile.error?.message || "Upload failed"}><ErrorIcon color="error" /></Tooltip>)
        : extendedFile.status === 'success'
            ? (<Tooltip title="Upload successful"><CheckCircleIcon color="success" /></Tooltip>)
            : (<InsertDriveFileIcon />)
}
