import { FC, useState } from "react";
import { Box, Chip, Divider, FormControlLabel, IconButton, Stack, Switch, Tooltip, Typography } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemSetUserManagement } from "./ProblemSetUserManagement";
import { ProblemSetProblemsEditorDialog } from "./ProblemSetProblemsEditorDialog";
import { useProblemSetUpdateSettingsMutation } from "@/features/problem-sets/api";
import { useAuthContext } from "@/features/auth/context";
import { InlineEditableText } from "./InlineEditableText";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { useCopyToClipboard } from "@/shared/hooks/useCopyToClipboard";
import { useTranslation } from "react-i18next";

const NAME_MAX = 50;
const DESCRIPTION_MAX = 255;

interface ProblemSetSettingsTabProps {
    problemSet: ProblemSet;
}

export const ProblemSetSettingsTab: FC<ProblemSetSettingsTabProps> = ({ problemSet }) => {
    const { t } = useTranslation("problem-sets");
    const { user } = useAuthContext();
    const updateSettingsMutation = useProblemSetUpdateSettingsMutation();
    const copyToClipboard = useCopyToClipboard();
    const [problemsEditorOpen, setProblemsEditorOpen] = useState(false);

    const total = problemSet.problems.length;

    const canEdit = !!user?.name && (problemSet.admins.includes(user.name) || problemSet.moderators.includes(user.name));

    const handleVisibilityToggle = () => {
        updateSettingsMutation.mutate({
            shareCode: problemSet.shareCode,
            data: { isPublic: !problemSet.isPublic },
        });
    };

    const saveName = (name: string) => {
        if (name === problemSet.name) return;
        updateSettingsMutation.mutate({ shareCode: problemSet.shareCode, data: { name } });
    };

    const saveDescription = (description: string) => {
        if (description === (problemSet.description ?? "")) return;
        updateSettingsMutation.mutate({ shareCode: problemSet.shareCode, data: { description } });
    };

    return (
        <Box sx={{ p: 3 }}>
            <Stack spacing={2}>
                {/* Summary Header */}
                <InlineEditableText
                    value={problemSet.name}
                    onSave={saveName}
                    disabled={!canEdit}
                    minLength={1}
                    maxLength={NAME_MAX}
                    editTooltip={t("edit_name_tooltip")}
                    typographyProps={{ variant: "h6", color: "primary" }}
                    pending={updateSettingsMutation.isPending}
                    endAdornment={
                        <Typography variant="body2" color="text.secondary" sx={{ flexShrink: 0 }}>
                            {t("problems_count", { count: total })}
                        </Typography>
                    }
                />

                <Divider />

                {/* Description */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("description_label")}
                    </Typography>
                    <InlineEditableText
                        value={problemSet.description ?? ""}
                        onSave={saveDescription}
                        disabled={!canEdit}
                        multiline
                        maxLength={DESCRIPTION_MAX}
                        editTooltip={t("edit_description_tooltip")}
                        emptyText={t("no_description")}
                        typographyProps={{ variant: "body2" }}
                        pending={updateSettingsMutation.isPending}
                    />
                </Box>

                <Divider />

                {/* Share Code + Visibility */}
                <Stack spacing={1.5}>
                    <Stack direction="row" alignItems="center" spacing={1}>
                        <Typography variant="body2" color="text.secondary">
                            {t("share_code_label")}
                        </Typography>
                        <Tooltip slotProps={defaultTooltipSlotProps} title={t("copy_share_code_tooltip")}>
                            <Chip
                                size="small"
                                icon={<ContentCopyIcon fontSize="small" />}
                                label={problemSet.shareCode}
                                onClick={() => {
                                    copyToClipboard(problemSet.shareCode);
                                }}
                                variant="outlined"
                            />
                        </Tooltip>
                    </Stack>

                    <Stack direction="row" alignItems="center" spacing={1}>
                        <Typography variant="body2" color="text.secondary">
                            {t("visibility_label")}
                        </Typography>
                        <FormControlLabel
                            control={
                                <Switch
                                    size="small"
                                    checked={problemSet.isPublic}
                                    onChange={handleVisibilityToggle}
                                    disabled={!canEdit || updateSettingsMutation.isPending}
                                />
                            }
                            label={
                                <Typography variant="body2">
                                    {problemSet.isPublic ? t("visibility_public") : t("visibility_private")}
                                </Typography>
                            }
                            sx={{ ml: 0 }}
                        />
                    </Stack>
                    <Typography variant="caption" color="text.secondary" sx={{ pl: 0.5 }}>
                        {problemSet.isPublic ? t("visibility_public_description") : t("visibility_private_description")}
                    </Typography>
                </Stack>

                <Divider />

                {/* Problems */}
                <Box>
                    <Stack direction="row" alignItems="center" justifyContent="space-between">
                        <Typography variant="overline" color="text.secondary">
                            {t("problems_label")} ({total})
                        </Typography>
                        <Tooltip slotProps={defaultTooltipSlotProps} title={t("edit_problems_tooltip")}>
                            <span>
                                <IconButton
                                    size="small"
                                    aria-label={t("edit_problems_tooltip")}
                                    onClick={() => {
                                        setProblemsEditorOpen(true);
                                    }}
                                    disabled={!canEdit}
                                >
                                    <EditOutlinedIcon fontSize="inherit" />
                                </IconButton>
                            </span>
                        </Tooltip>
                    </Stack>
                </Box>

                <Divider />

                {/* Users */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("users_label")}
                    </Typography>
                    <ProblemSetUserManagement shareCode={problemSet.shareCode} />
                </Box>
            </Stack>

            <ProblemSetProblemsEditorDialog
                problemSet={problemSet}
                open={problemsEditorOpen}
                onClose={() => {
                    setProblemsEditorOpen(false);
                }}
            />
        </Box>
    );
};
