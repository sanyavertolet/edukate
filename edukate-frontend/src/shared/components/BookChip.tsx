import { FC } from "react";
import { Box } from "@mui/material";

type BookChipProps = {
    bookSlug: string;
    onClick?: (bookSlug: string) => void;
};

export const BookChip: FC<BookChipProps> = ({ bookSlug, onClick }) => {
    return (
        <Box
            component="span"
            sx={{
                color: "primary.main",
                cursor: onClick ? "pointer" : "default",
                "&:hover": onClick ? { textDecoration: "underline" } : undefined,
            }}
            onClick={
                onClick
                    ? (e) => {
                          e.stopPropagation();
                          onClick(bookSlug);
                      }
                    : undefined
            }
        >
            {bookSlug}
        </Box>
    );
};
