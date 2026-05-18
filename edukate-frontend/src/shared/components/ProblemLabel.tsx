import { FC } from "react";
import { Box } from "@mui/material";

type ProblemLabelProps = {
    problemKey: string;
    onClick?: (problemKey: string) => void;
};

export const ProblemLabel: FC<ProblemLabelProps> = ({ problemKey, onClick }) => {
    const code = problemKey.split("/").slice(1).join("/");
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
                          onClick(problemKey);
                      }
                    : undefined
            }
        >
            {code}
        </Box>
    );
};
