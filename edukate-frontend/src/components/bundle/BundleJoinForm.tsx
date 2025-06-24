import { Divider, IconButton, InputBase, Paper, Tooltip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';
import { FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useJoinBundleMutation } from "../../http/requests";
import { defaultTooltipSlotProps } from "../../utils/utils";

export const BundleJoinForm: FC = () => {
    const navigate = useNavigate();
    const joinBundleMutation = useJoinBundleMutation();
    const [bundleCode, setBundleCode] = useState("");

    const requestOptions = {
        onSuccess: () => navigate(`/bundles/${bundleCode}`),
        onError: () => { joinBundleMutation.reset(); setBundleCode(""); }
    };

    const onSearchClick = () => joinBundleMutation.mutate(bundleCode, requestOptions);
    const onCreateClick = () => navigate("/bundles/new");
    return (
        <Paper component="form" sx={{ p: '0px 1px', display: 'flex', alignItems: 'center' }}>
            <InputBase sx={{ ml: 1, flex: 1 }} placeholder="Join by code" inputProps={{ 'aria-label': 'join by code' }}
                       value={bundleCode} onChange={(e) => setBundleCode(e.target.value)}
            />
            <IconButton color="default" aria-label="join" disabled={ bundleCode.length == 0 } onClick={onSearchClick}>
                <GroupOutlinedIcon />
            </IconButton>
            <Divider sx={{ height: "1.75rem" }} orientation="vertical" />
            <IconButton color="secondary" aria-label="create" onClick={onCreateClick}>
                <Tooltip title="Create a bundle" slotProps={defaultTooltipSlotProps}>
                    <AddIcon/>
                </Tooltip>
            </IconButton>
        </Paper>
    );
};
