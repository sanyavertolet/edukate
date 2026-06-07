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
    public: {
        title: "public_empty_title",
        description: "public_empty_description",
    },
    user: {
        title: "user_empty_title",
        description: "user_empty_description",
        cta: "user_empty_cta",
    },
    moderator: {
        title: "moderator_empty_title",
        description: "moderator_empty_description",
    },
    admin: {
        title: "admin_empty_title",
        description: "admin_empty_description",
        cta: "admin_empty_cta",
    },
};

export function ProblemSetEmptyState({ tab, onTabSwitch }: ProblemSetEmptyStateProps) {
    const { t } = useTranslation("problem-sets");
    const navigate = useNavigate();
    const { title, description, cta } = emptyStateKeys[tab];

    const handleAction = () => {
        if (tab === "admin") {
            void navigate("/problem-sets/new");
        } else if (tab === "user" && onTabSwitch) {
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
