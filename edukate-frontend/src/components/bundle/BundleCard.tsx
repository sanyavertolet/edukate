import { BundleMetadata } from "../../types/BundleMetadata";
import { FC } from "react";
import {
    Avatar, AvatarGroup,
    Box,
    Button,
    Card,
    CardActions,
    CardContent,
    Chip, Divider,
    Tooltip,
    Typography
} from "@mui/material";
import StorageIcon from "@mui/icons-material/Storage";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { useNavigate } from "react-router-dom";
import { PublicityIcon } from "./PublicityIcon";

interface BundleCardProps {
    bundleMetadata: BundleMetadata;
    onCopy?: () => void;
}

const getAvatarColor = (name: string) => {
    const colors = [
        "#F44336",
        "#E91E63",
        "#9C27B0",
        "#673AB7",
        "#3F51B5",
        "#2196F3",
        "#03A9F4",
        "#00BCD4",
        "#009688",
        "#4CAF50",
        "#8BC34A",
        "#CDDC39",
    ]

    let hash = 0
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash)
    }

    return colors[Math.abs(hash) % colors.length]
}

export const BundleCard: FC<BundleCardProps> = ({bundleMetadata, onCopy}) => {

    const copyShareCode = () => {
        navigator.clipboard.writeText(bundleMetadata.shareCode).finally(onCopy);
    };

    const getInitials = (username: string) => username.substring(0, 2);

    const navigate = useNavigate();

    return (
        <Card sx={{
            display: 'flex',
            flexDirection: 'column',
            m: "1rem",
        }}>
            <CardContent sx={{ pb: 0 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="h5" component="div">{ bundleMetadata.name }</Typography>
                    <PublicityIcon isPublic={bundleMetadata.isPublic}/>
                </Box>

                <Typography variant="body2" color="text.secondary" sx={{
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                }}>{ bundleMetadata.description || "No description provided" }</Typography>

                <Divider sx={{ my: 1 }} />

                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
                    <Tooltip title="Bundle size">
                        {/*sx={{ display: 'flex', alignItems: 'center', mr: 2 }}*/}
                        <Chip variant={"outlined"} icon={<StorageIcon fontSize="small" />} label={`${bundleMetadata.size} problems`}/>
                    </Tooltip>

                    <Tooltip title="Share code">
                        <Chip variant={"outlined"} icon={<ContentCopyIcon fontSize="small" />} label={bundleMetadata.shareCode} onClick={copyShareCode}/>
                    </Tooltip>
                </Box>

                <Divider sx={{ my: 1 }} />

                <Typography variant="subtitle2" component="div">
                    Administrators:
                </Typography>

                <Box sx={{ display: "flex", alignItems: "center" }}>
                    <AvatarGroup>
                        {bundleMetadata.admins.map((admin) => (
                            <Tooltip key={`${admin}-tooltip`} title={admin}>
                                <Avatar key={`${admin}-avatar`} sx={{ backgroundColor: getAvatarColor(admin) }}>
                                    {getInitials(admin)}
                                </Avatar>
                            </Tooltip>
                        ))}
                    </AvatarGroup>

                    {bundleMetadata.admins.length > 4 && (
                        <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                            +{bundleMetadata.admins.length - 4} more
                        </Typography>
                    )}
                </Box>
            </CardContent>

            <CardActions disableSpacing>
                <Button sx={{marginLeft: "auto"}} size="small" onClick={() => navigate(`/bundles/${bundleMetadata.shareCode}`)}>
                    View
                </Button>
            </CardActions>
        </Card>
    );
};
