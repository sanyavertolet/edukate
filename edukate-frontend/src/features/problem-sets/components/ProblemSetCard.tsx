import { ProblemSetMetadata } from "@/features/problem-sets/types";
import { FC } from "react";
import {
    Avatar,
    AvatarGroup,
    Box,
    Button,
    Card,
    CardActions,
    CardContent,
    Chip,
    Divider,
    Tooltip,
    Typography,
} from "@mui/material";
import StorageIcon from "@mui/icons-material/Storage";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { useNavigate } from "react-router-dom";
import { PublicityIcon } from "./PublicityIcon";
import { defaultTooltipSlotProps, getColorByStringHash, getFirstLetters } from "@/shared/utils/utils";

interface ProblemSetCardProps {
    problemSetMetadata: ProblemSetMetadata;
    onCopy?: () => void;
}

export const ProblemSetCard: FC<ProblemSetCardProps> = ({ problemSetMetadata, onCopy }) => {
    const copyShareCode = () => {
        void navigator.clipboard.writeText(problemSetMetadata.shareCode).finally(onCopy);
    };
    const navigate = useNavigate();
    const navigateTo = (problemSetMetadata: ProblemSetMetadata) => {
        void navigate(`/problem-sets/${problemSetMetadata.shareCode}`);
    };
    return (
        <Card sx={{ display: "flex", flexDirection: "column" }} key={problemSetMetadata.shareCode}>
            <CardContent sx={{ pb: 0 }}>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1 }}>
                    <Typography variant="h5" component="div">
                        {problemSetMetadata.name}
                    </Typography>
                    <PublicityIcon isPublic={problemSetMetadata.isPublic} />
                </Box>

                <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                        display: "-webkit-box",
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: "vertical",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                    }}
                >
                    {problemSetMetadata.description || "No description provided"}
                </Typography>

                <Divider sx={{ my: 1 }} />

                <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                    <Tooltip slotProps={defaultTooltipSlotProps} title="Problem set size">
                        <Chip
                            variant={"outlined"}
                            icon={<StorageIcon fontSize="small" />}
                            label={`${String(problemSetMetadata.size)} problems`}
                        />
                    </Tooltip>

                    <Tooltip slotProps={defaultTooltipSlotProps} title="Share code">
                        <Chip
                            variant={"outlined"}
                            icon={<ContentCopyIcon fontSize="small" />}
                            label={problemSetMetadata.shareCode}
                            aria-label={`Copy share code ${problemSetMetadata.shareCode}`}
                            onClick={copyShareCode}
                        />
                    </Tooltip>
                </Box>

                <Divider sx={{ my: 1 }} />

                <Tooltip slotProps={defaultTooltipSlotProps} title={"Administrators"}>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <AvatarGroup>
                            {problemSetMetadata.admins.map((admin) => (
                                <Tooltip slotProps={defaultTooltipSlotProps} key={`${admin}-tooltip`} title={admin}>
                                    <Avatar key={`${admin}-avatar`} sx={{ backgroundColor: getColorByStringHash(admin) }}>
                                        {getFirstLetters(admin, 2)}
                                    </Avatar>
                                </Tooltip>
                            ))}
                        </AvatarGroup>

                        {problemSetMetadata.admins.length > 4 && (
                            <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                                +{problemSetMetadata.admins.length - 4} more
                            </Typography>
                        )}
                    </Box>
                </Tooltip>
            </CardContent>

            <CardActions disableSpacing>
                <Button
                    sx={{ marginLeft: "auto" }}
                    size="small"
                    onClick={() => {
                        navigateTo(problemSetMetadata);
                    }}
                >
                    View
                </Button>
            </CardActions>
        </Card>
    );
};
