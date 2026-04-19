import { Divider, IconButton, InputBase, Paper, Tooltip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import GroupOutlinedIcon from "@mui/icons-material/GroupOutlined";
import { FC } from "react";
import { useNavigate } from "react-router-dom";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { ConditionalTooltip } from "@/shared/components/ConditionalTooltip";

interface ProblemSetJoinFormProps {
    disabled?: boolean;
}

export const ProblemSetJoinForm: FC<ProblemSetJoinFormProps> = ({ disabled = false }) => {
    const navigate = useNavigate();

    const onCreateClick = () => {
        void navigate("/problem-sets/new");
    };
    return (
        <ConditionalTooltip title={"Sign in to use problem sets"} shown={disabled} placement={"bottom-end"}>
            <Paper component="form" sx={{ p: "0px 1px", display: "flex", alignItems: "center" }}>
                <Tooltip title="Join by invite link — coming soon" slotProps={defaultTooltipSlotProps}>
                    <span>
                        <InputBase
                            sx={{ ml: 1, flex: 1 }}
                            placeholder="Join by code"
                            inputProps={{ "aria-label": "join by code" }}
                            disabled
                        />
                        <IconButton color="default" aria-label="join" disabled>
                            <GroupOutlinedIcon />
                        </IconButton>
                    </span>
                </Tooltip>
                <Divider sx={{ height: "1.75rem" }} orientation="vertical" />
                <IconButton color="secondary" aria-label="create" onClick={onCreateClick} disabled={disabled}>
                    <Tooltip title="Create a problem set" slotProps={defaultTooltipSlotProps}>
                        <AddIcon />
                    </Tooltip>
                </IconButton>
            </Paper>
        </ConditionalTooltip>
    );
};
