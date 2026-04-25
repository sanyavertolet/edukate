import { Button, Stack, Typography } from "@mui/material";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import { useNavigate } from "react-router-dom";
import { ProblemSetCategory } from "@/features/problem-sets/types";

interface ProblemSetEmptyStateProps {
    tab: ProblemSetCategory;
    onTabSwitch?: (tab: ProblemSetCategory) => void;
}

const emptyStateConfig: Record<ProblemSetCategory, { title: string; description: string; cta?: string }> = {
    owned: {
        title: "You haven't created any problem sets yet.",
        description: "Create your first problem set to organize and share problems with others.",
        cta: "Create Problem Set",
    },
    joined: {
        title: "You haven't joined any problem sets yet.",
        description: "Ask for a share code or browse public problem sets to get started.",
        cta: "Browse Public",
    },
    public: {
        title: "No public problem sets available yet.",
        description: "Be the first to create and share a public problem set!",
    },
};

export function ProblemSetEmptyState({ tab, onTabSwitch }: ProblemSetEmptyStateProps) {
    const navigate = useNavigate();
    const { title, description, cta } = emptyStateConfig[tab];

    const handleAction = () => {
        if (tab === "owned") {
            void navigate("/problem-sets/new");
        } else if (tab === "joined" && onTabSwitch) {
            onTabSwitch("public");
        }
    };

    return (
        <Stack alignItems="center" spacing={2} sx={{ py: 6 }}>
            <InboxOutlinedIcon sx={{ fontSize: 48, color: "text.secondary" }} />
            <Typography variant="h6" color="text.primary">
                {title}
            </Typography>
            <Typography variant="body2" color="text.secondary" textAlign="center" maxWidth={400}>
                {description}
            </Typography>
            {cta && (
                <Button variant="outlined" onClick={handleAction}>
                    {cta}
                </Button>
            )}
        </Stack>
    );
}
