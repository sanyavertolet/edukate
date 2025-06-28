import { BundleMetadata } from "../../types/bundle/BundleMetadata";
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
import { defaultTooltipSlotProps, getColorByStringHash, getFirstLetters } from "../../utils/utils"

interface BundleCardProps {
    bundleMetadata: BundleMetadata;
    onCopy?: () => void;
}

export const BundleCard: FC<BundleCardProps> = ({bundleMetadata, onCopy}) => {
    const copyShareCode = () => { navigator.clipboard.writeText(bundleMetadata.shareCode).finally(onCopy); };
    const navigate = useNavigate();
    const navigateTo = (bundleMetadata: BundleMetadata) => navigate(`/bundles/${bundleMetadata.shareCode}`);
    return (
        <Card sx={{ display: 'flex', flexDirection: 'column' }} key={ bundleMetadata.shareCode }>
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
                    <Tooltip slotProps={defaultTooltipSlotProps} title="Bundle size">
                        {/*sx={{ display: 'flex', alignItems: 'center', mr: 2 }}*/}
                        <Chip variant={"outlined"} icon={<StorageIcon fontSize="small" />} label={`${bundleMetadata.size} problems`}/>
                    </Tooltip>

                    <Tooltip slotProps={defaultTooltipSlotProps} title="Share code">
                        <Chip variant={"outlined"} icon={<ContentCopyIcon fontSize="small" />} label={bundleMetadata.shareCode} onClick={copyShareCode}/>
                    </Tooltip>
                </Box>

                <Divider sx={{ my: 1 }} />

                <Tooltip slotProps={defaultTooltipSlotProps} title={"Administrators"}>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <AvatarGroup>
                            {bundleMetadata.admins.map((admin) => (
                                <Tooltip slotProps={defaultTooltipSlotProps} key={`${admin}-tooltip`} title={admin}>
                                    <Avatar key={`${admin}-avatar`} sx={{ backgroundColor: getColorByStringHash(admin) }}>
                                        {getFirstLetters(admin, 2)}
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
                </Tooltip>
            </CardContent>

            <CardActions disableSpacing>
                <Button sx={{marginLeft: "auto"}} size="small" onClick={() => navigateTo(bundleMetadata)}>
                    View
                </Button>
            </CardActions>
        </Card>
    );
};
