import { UtcIsoString } from "../common/DateTypes";

export interface FileMetadata {
    key: string;
    authorName: string;
    lastModified: UtcIsoString;
    size: number;

    status?: 'pending' | 'uploading' | 'success' | 'error';
    progress?: number;
    error?: Error;
    _file?: File;
}
