import { useState, useEffect, useRef, ChangeEvent } from "react";
import { usePostTempFileMutation, useDeleteTempFileMutation, useGetTempFiles } from "@/features/files/api";
import { FileMetadata } from "@/features/files/types";
import { formatFileSize } from "@/shared/utils/utils";
import { nowUtcIso } from "@/shared/utils/date";

type UseFileUploadProps = {
    onTempFileUploaded: (fileKey: string) => void;
    onTempFileDeleted: (fileKey: string) => void;
    maxFiles?: number;
    maxSize?: number;
};

export const useFileUpload = ({
    onTempFileUploaded,
    onTempFileDeleted,
    maxFiles = 5,
    maxSize = 50 * 1024 * 1024,
}: UseFileUploadProps) => {
    const [fileMetadataList, setFileMetadataList] = useState<FileMetadata[]>([]);
    const [selectedFileKey, setSelectedFileKey] = useState<string>();
    const [previewDialogOpen, setPreviewDialogOpen] = useState(false);
    const [errorText, setErrorText] = useState<string>();

    // Keep refs to the latest callbacks so effects and async callbacks never close over stale values.
    const onTempFileUploadedRef = useRef(onTempFileUploaded);
    const onTempFileDeletedRef = useRef(onTempFileDeleted);
    useEffect(() => {
        onTempFileUploadedRef.current = onTempFileUploaded;
        onTempFileDeletedRef.current = onTempFileDeleted;
    });

    const { data: tempFiles, isLoading, error } = useGetTempFiles();
    const postTempFileMutation = usePostTempFileMutation();
    const deleteTempFileMutation = useDeleteTempFileMutation();

    useEffect(() => {
        if (tempFiles && tempFiles.length > 0 && !isLoading && !error) {
            setFileMetadataList((prev) => {
                const newFiles = tempFiles.filter(
                    (tempFile) => !prev.some((existingFile) => existingFile.key === tempFile.key),
                );

                const filesWithStatus = newFiles.map((file) => ({
                    ...file,
                    status: "success" as const,
                    progress: 100,
                }));

                return [...prev, ...filesWithStatus];
            });

            for (const file of tempFiles) {
                if (file.key) {
                    onTempFileUploadedRef.current(file.key);
                }
            }
        }
    }, [tempFiles, isLoading, error]);

    const handleFileClick = (key: string) => {
        setSelectedFileKey(key);
        setPreviewDialogOpen(true);
    };

    const handleClosePreview = () => {
        setPreviewDialogOpen(false);
        setSelectedFileKey(undefined);
    };

    const handleAddFiles = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files) return;

        const newFiles: FileMetadata[] = Array.from(event.target.files).map((file) => ({
            key: file.name,
            authorName: "",
            lastModified: nowUtcIso(),
            size: file.size,
            status: "pending",
            progress: 0,
            _file: file,
        }));

        const currentFilesLength = fileMetadataList.length + event.target.files.length;
        if (currentFilesLength > maxFiles) {
            setErrorText(`You can upload no more than ${maxFiles} files. You got ${currentFilesLength}.`);
            return;
        }

        const oldSize = fileMetadataList.reduce((sum, metadata) => sum + metadata.size, 0);
        const newSize = newFiles.reduce((sum, metadata) => sum + metadata.size, 0);
        if (oldSize + newSize > maxSize) {
            setErrorText(
                `You can upload no more than ${formatFileSize(maxSize)}, you have ${formatFileSize(oldSize + newSize)}.`,
            );
            return;
        }

        setFileMetadataList((prev) => [...prev, ...newFiles]);

        for (const metadata of newFiles) {
            if (!metadata._file) continue;
            const file = metadata._file;

            setFileMetadataList((prev) => prev.map((m) => (m.key === metadata.key ? { ...m, status: "uploading" } : m)));

            postTempFileMutation.mutate(
                {
                    file,
                    onProgress: (progress) => {
                        setFileMetadataList((prev) =>
                            prev.map((m) => (m.key === metadata.key ? { ...m, progress, status: "uploading" } : m)),
                        );
                    },
                },
                {
                    onSuccess: (key) => {
                        setFileMetadataList((prev) =>
                            prev.map((m) =>
                                m.key === metadata.key ? { ...m, key, status: "success", progress: 100 } : m,
                            ),
                        );
                        onTempFileUploadedRef.current(key);
                    },
                    onError: (error) => {
                        setFileMetadataList((prev) =>
                            prev.map((m) =>
                                m.key === metadata.key
                                    ? { ...m, status: "error", error: error as Error, progress: 0 }
                                    : m,
                            ),
                        );
                    },
                },
            );
        }

        if (event.target) {
            event.target.value = "";
        }
    };

    const handleRemoveFile = (key: string) => {
        const file = fileMetadataList.find((file) => file.key === key);
        if (file && file.status === "success") {
            deleteTempFileMutation.mutate(key, {
                onSuccess: (serverKey) => {
                    onTempFileDeletedRef.current(serverKey);
                    setFileMetadataList((prev) => prev.filter((file) => file.key !== serverKey));
                },
            });
        } else {
            setFileMetadataList((prev) => prev.filter((file) => file.key !== key));
        }
    };

    return {
        fileMetadataList,
        selectedFileKey,
        previewDialogOpen,
        errorText,
        handleFileClick,
        handleClosePreview,
        handleAddFiles,
        handleRemoveFile,
        setErrorText,
    };
};
