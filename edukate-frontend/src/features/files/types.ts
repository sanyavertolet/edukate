import type { FileMetadata as FileMetadataDto } from "@/generated/backend";

export interface FileMetadata extends FileMetadataDto {
    status?: "pending" | "uploading" | "success" | "error";
    progress?: number;
    error?: Error;
    _file?: File;
}
