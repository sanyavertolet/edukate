export interface ExtendedFile {
    content: File;
    status: 'pending' | 'uploading' | 'success' | 'error';
    progress: number;
    key?: string;
    error?: Error;
}
