import { Tooltip } from "@mui/material";
import ErrorIcon from "@mui/icons-material/Error";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile";
import { FileMetadata } from "../../types/FileMetadata";

interface FileStatusIconProps {
    file: FileMetadata;
}

export function FileStatusIcon({ file }: FileStatusIconProps) {
    return file.status === 'error'
        ? (<Tooltip title={file.error?.message || "Upload failed"}><ErrorIcon color="error" /></Tooltip>)
        : file.status === 'success'
            ? (<Tooltip title="Upload successful"><CheckCircleIcon color="success" /></Tooltip>)
            : (<InsertDriveFileIcon />)
}
