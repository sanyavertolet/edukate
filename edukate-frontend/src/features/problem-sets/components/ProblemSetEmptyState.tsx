import { Button, Stack, Typography } from "@mui/material";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import { useNavigate } from "react-router-dom";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { useTranslation } from "react-i18next";

interface ProblemSetEmptyStateProps {
    tab: ProblemSetCategory;
    onTabSwitch?: (tab: ProblemSetCategory) => void;
}

const emptyStateKeys: Record<ProblemSetCategory, { title: string; description: string; cta?: string }> = {
    owned: {
        title: "owned_empty_title",
        description: "owned_empty_description",
        cta: "owned_empty_cta",
    },
    joined: {
        title: "joined_empty_title",
        description: "joined_empty_description",
        cta: "joined_empty_cta",
    },
    public: {
        title: "public_empty_title",
        description: "public_empty_description",
    },
};

export function ProblemSetEmptyState({ tab, onTabSwitch }: ProblemSetEmptyStateProps) {
    const { t } = useTranslation("problem-sets");
    const navigate = useNavigate();
    const { title, description, cta } = emptyStateKeys[tab];

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
                {t(title)}
            </Typography>
            <Typography variant="body2" color="text.secondary" textAlign="center" maxWidth={400}>
                {t(description)}
            </Typography>
            {cta && (
                <Button variant="outlined" onClick={handleAction}>
                    {t(cta)}
                </Button>
            )}
        </Stack>
    );
}
