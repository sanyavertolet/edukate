import { UtcIsoString } from "@/shared/utils/date-types";

export interface FileMetadata {
    key: string;
    authorName: string;
    lastModified: UtcIsoString;
    size: number;

    status?: "pending" | "uploading" | "success" | "error";
    progress?: number;
    error?: Error;
    _file?: File;
}

export interface ExtendedFile {
    content: File;
    status: "pending" | "uploading" | "success" | "error";
    progress: number;
    key?: string;
    error?: Error;
}
