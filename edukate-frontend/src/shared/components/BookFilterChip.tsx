import { FC } from "react";
import { Box, Chip } from "@mui/material";

type BookFilterChipProps = {
    bookSlug: string;
    onClear: () => void;
};

export const BookFilterChip: FC<BookFilterChipProps> = ({ bookSlug, onClear }) => {
    if (!bookSlug) return null;

    return (
        <Box sx={{ px: 2, pt: 1, pb: 1, display: "flex", justifyContent: "flex-start" }}>
            <Chip label={`Book: ${bookSlug}`} onDelete={onClear} color="primary" variant="outlined" size="small" />
        </Box>
    );
};
