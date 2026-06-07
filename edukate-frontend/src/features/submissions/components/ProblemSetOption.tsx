import { ElementType, FC, HTMLAttributes, MouseEvent } from "react";
import { Box, Collapse, IconButton, LinearProgress, ListItem, Stack, Tooltip, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import WorkspacePremiumIcon from "@mui/icons-material/WorkspacePremium";
import ShieldIcon from "@mui/icons-material/Shield";
import PersonIcon from "@mui/icons-material/Person";
import { useTranslation } from "react-i18next";
import { ProblemSetMetadata } from "@/features/problem-sets/types";
import { ProblemSetMetadataCurrentUserRole } from "@/generated/backend";
import { PublicityIcon } from "@/features/problem-sets/components/PublicityIcon";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { colorForRole } from "./roleColor";

const ROLE_ICON: Record<ProblemSetMetadataCurrentUserRole, ElementType> = {
    ADMIN: WorkspacePremiumIcon,
    MODERATOR: ShieldIcon,
    USER: PersonIcon,
};

const ROLE_LABEL_KEY: Record<ProblemSetMetadataCurrentUserRole, string> = {
    ADMIN: "role_admin",
    MODERATOR: "role_moderator",
    USER: "role_user",
};

type ProblemSetOptionProps = {
    option: ProblemSetMetadata;
    isExpanded: boolean;
    onToggleExpand: (e: MouseEvent) => void;
    listItemProps: HTMLAttributes<HTMLLIElement>;
};

export const ProblemSetOption: FC<ProblemSetOptionProps> = ({ option, isExpanded, onToggleExpand, listItemProps }) => {
    const { t } = useTranslation("problem-sets");
    const roleColor = colorForRole(option.currentUserRole);
    const RoleIcon = option.currentUserRole ? ROLE_ICON[option.currentUserRole] : null;
    const roleLabel = option.currentUserRole ? t(ROLE_LABEL_KEY[option.currentUserRole]) : undefined;
    const percentage = option.size > 0 ? (option.solvedCount / option.size) * 100 : 0;
    const isComplete = option.size > 0 && option.solvedCount === option.size;
    const adminLabel =
        option.admins.length > 1
            ? t("by_admin_plus", { admin: option.admins[0], count: option.admins.length - 1 })
            : option.admins.length === 1
              ? t("by_admin", { admin: option.admins[0] })
              : undefined;

    return (
        <ListItem {...listItemProps} sx={{ px: 1.5, py: 0.75 }}>
            <Box sx={{ display: "flex", flexDirection: "column", gap: 0.25, width: "100%", minWidth: 0 }}>
                <Stack direction="row" alignItems="center" spacing={1}>
                    <PublicityIcon isPublic={option.isPublic} disableTooltip />
                    <Typography
                        variant="body2"
                        noWrap
                        sx={{
                            color: roleColor,
                            fontWeight: option.currentUserRole === "ADMIN" ? 600 : 500,
                            flexShrink: 1,
                            minWidth: 0,
                        }}
                    >
                        {option.name}
                    </Typography>
                    {RoleIcon && roleLabel && (
                        <Tooltip slotProps={defaultTooltipSlotProps} title={roleLabel}>
                            <RoleIcon sx={{ fontSize: 16, color: roleColor, flexShrink: 0 }} />
                        </Tooltip>
                    )}
                    <Box sx={{ flexGrow: 1 }} />
                    {option.size > 0 && (
                        <Stack direction="row" alignItems="center" spacing={0.75} sx={{ flexShrink: 0 }}>
                            <LinearProgress
                                variant="determinate"
                                value={percentage}
                                color={isComplete ? "success" : "primary"}
                                sx={{ width: 40, height: 4, borderRadius: 2 }}
                            />
                            <Typography variant="caption" color="text.secondary" noWrap>
                                {t("solved_count", { solved: option.solvedCount, total: option.size })}
                            </Typography>
                        </Stack>
                    )}
                    {option.description && (
                        <IconButton
                            size="small"
                            onClick={onToggleExpand}
                            sx={{
                                ml: 0.5,
                                p: 0.25,
                                transform: isExpanded ? "rotate(180deg)" : "rotate(0deg)",
                                transition: "transform 0.2s",
                                flexShrink: 0,
                            }}
                        >
                            <ExpandMoreIcon sx={{ fontSize: 16 }} />
                        </IconButton>
                    )}
                </Stack>
                {adminLabel && (
                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", pl: 4 }} noWrap>
                        {adminLabel}
                    </Typography>
                )}
                {option.description && (
                    <Collapse in={isExpanded}>
                        <Typography variant="caption" color="text.secondary" sx={{ display: "block", pl: 4, pt: 0.5 }}>
                            {option.description}
                        </Typography>
                    </Collapse>
                )}
            </Box>
        </ListItem>
    );
};
