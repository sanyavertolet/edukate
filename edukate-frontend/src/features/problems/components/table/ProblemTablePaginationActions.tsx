import { FC, KeyboardEvent, useEffect, useState } from "react";
import { Box, IconButton, TextField, Typography } from "@mui/material";
import { FirstPage, KeyboardArrowLeft, KeyboardArrowRight, LastPage } from "@mui/icons-material";

type Props = {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (e: unknown, newPage: number) => void;
};

export const ProblemTablePaginationActions: FC<Props> = ({ count, page, rowsPerPage, onPageChange }) => {
    const pageCount = Math.ceil(count / rowsPerPage);
    const [inputValue, setInputValue] = useState(String(page + 1));

    useEffect(() => {
        setInputValue(String(page + 1));
    }, [page]);

    const goToPage = (raw: string) => {
        const parsed = parseInt(raw, 10);
        if (isNaN(parsed)) {
            setInputValue(String(page + 1));
            return;
        }
        const clamped = Math.max(1, Math.min(parsed, pageCount));
        setInputValue(String(clamped));
        if (clamped - 1 !== page) {
            onPageChange(null, clamped - 1);
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            goToPage(inputValue);
        }
    };

    return (
        <Box
            sx={{
                display: "flex",
                alignItems: "center",
                gap: 0.5,
            }}
        >
            <IconButton
                onClick={(e) => {
                    onPageChange(e, 0);
                }}
                disabled={page === 0}
                size="small"
            >
                <FirstPage fontSize="small" />
            </IconButton>
            <IconButton
                onClick={(e) => {
                    onPageChange(e, page - 1);
                }}
                disabled={page === 0}
                size="small"
            >
                <KeyboardArrowLeft fontSize="small" />
            </IconButton>

            <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 0.5, whiteSpace: "nowrap" }}>
                <TextField
                    value={inputValue}
                    onChange={(e) => {
                        setInputValue(e.target.value);
                    }}
                    onBlur={() => {
                        goToPage(inputValue);
                    }}
                    onKeyDown={handleKeyDown}
                    size="small"
                    slotProps={{ htmlInput: { min: 1, max: pageCount, style: { textAlign: "center", padding: "4px 6px" } } }}
                    sx={{ width: 52 }}
                />
                <Typography variant="body2" color="text.secondary">
                    / {pageCount}
                </Typography>
            </Box>

            <IconButton
                onClick={(e) => {
                    onPageChange(e, page + 1);
                }}
                disabled={page >= pageCount - 1}
                size="small"
            >
                <KeyboardArrowRight fontSize="small" />
            </IconButton>
            <IconButton
                onClick={(e) => {
                    onPageChange(e, pageCount - 1);
                }}
                disabled={page >= pageCount - 1}
                size="small"
            >
                <LastPage fontSize="small" />
            </IconButton>
        </Box>
    );
};
