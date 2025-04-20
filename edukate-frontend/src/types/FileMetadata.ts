export interface FileMetadata {
    key: string;
    authorName: string;
    lastModified: string;
    size: number;

    status?: 'pending' | 'uploading' | 'success' | 'error';
    progress?: number;
    error?: Error;
    _file?: File;
}
