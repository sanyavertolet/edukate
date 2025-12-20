import { Divider, IconButton, InputBase, Paper, Tooltip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';
import { FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useJoinBundleMutation } from "../../http/requests/bundles";
import { defaultTooltipSlotProps } from "../../utils/utils";
import { ConditionalTooltip } from "../basic/ConditionalTooltip";
import { toast } from "react-toastify";

interface BundleJoinFormProps {
    disabled?: boolean;
}

export const BundleJoinForm: FC<BundleJoinFormProps> = ({disabled = false}) => {
    const navigate = useNavigate();
    const joinBundleMutation = useJoinBundleMutation();
    const [bundleCode, setBundleCode] = useState("");

    const requestOptions = {
        onSuccess: () => navigate(`/bundles/${bundleCode}`),
        onError: () => {
            joinBundleMutation.reset();
            setBundleCode("");
            toast.error("Could not join a bundle");
        }
    };

    const onSearchClick = () => joinBundleMutation.mutate(bundleCode, requestOptions);
    const onCreateClick = () => navigate("/bundles/new");
    return (
        <ConditionalTooltip title={ "Sign in to use bundles" } shown={disabled} placement={"right"}>
            <Paper component="form" sx={{ p: '0px 1px', display: 'flex', alignItems: 'center' }}>
                <InputBase sx={{ ml: 1, flex: 1 }} placeholder="Join by code"
                           inputProps={{ 'aria-label': 'join by code' }} value={bundleCode} disabled={disabled}
                           onChange={(e) => setBundleCode(e.target.value)}
                />
                <IconButton color="default" aria-label="join" onClick={onSearchClick}
                            disabled={ disabled || bundleCode.length == 0 }>
                    <GroupOutlinedIcon />
                </IconButton>
                <Divider sx={{ height: "1.75rem" }} orientation="vertical" />
                <IconButton color="secondary" aria-label="create" onClick={onCreateClick} disabled={disabled}>
                    <Tooltip title="Create a bundle" slotProps={defaultTooltipSlotProps}>
                        <AddIcon/>
                    </Tooltip>
                </IconButton>
            </Paper>
        </ConditionalTooltip>
    );
};
