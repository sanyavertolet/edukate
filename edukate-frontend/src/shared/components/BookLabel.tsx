import { FC } from "react";
import { Box } from "@mui/material";

type BookLabelProps = {
    bookSlug: string;
    onClick?: (bookSlug: string) => void;
};

export const BookLabel: FC<BookLabelProps> = ({ bookSlug, onClick }) => {
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
