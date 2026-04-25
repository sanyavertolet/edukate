import { Box, Button, Divider, IconButton, InputBase, Paper, Stack, Tooltip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import GroupOutlinedIcon from "@mui/icons-material/GroupOutlined";
import { FC } from "react";
import { useNavigate } from "react-router-dom";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { ConditionalTooltip } from "@/shared/components/ConditionalTooltip";

interface ProblemSetToolbarProps {
    disabled?: boolean;
}

export const ProblemSetToolbar: FC<ProblemSetToolbarProps> = ({ disabled = false }) => {
    const navigate = useNavigate();

    const onCreateClick = () => {
        void navigate("/problem-sets/new");
    };

    return (
        <Stack
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "stretch", sm: "center" }}
            spacing={1}
        >
            <Tooltip title="Join by invite link — coming soon" slotProps={defaultTooltipSlotProps}>
                <Paper sx={{ p: "2px 4px", display: "flex", alignItems: "center", flex: { sm: 1 }, maxWidth: { sm: 360 } }}>
                    <InputBase
                        sx={{ ml: 1, flex: 1 }}
                        placeholder="Join by code"
                        inputProps={{ "aria-label": "join by code" }}
                        disabled
                    />
                    <Divider sx={{ height: "1.75rem" }} orientation="vertical" />
                    <IconButton color="default" aria-label="join" disabled>
                        <GroupOutlinedIcon />
                    </IconButton>
                </Paper>
            </Tooltip>

            <ConditionalTooltip title="Sign in to create problem sets" shown={disabled} placement="bottom-end">
                <Box>
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={onCreateClick}
                        disabled={disabled}
                        fullWidth
                    >
                        Create
                    </Button>
                </Box>
            </ConditionalTooltip>
        </Stack>
    );
};
