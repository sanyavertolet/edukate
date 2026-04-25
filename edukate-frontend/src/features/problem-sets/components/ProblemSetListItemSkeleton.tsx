import { Box, ListItem, Skeleton, Stack } from "@mui/material";

export function ProblemSetListItemSkeleton() {
    return (
        <ListItem sx={{ py: 1.5, px: 2 }}>
            <Box sx={{ display: "flex", flexDirection: "column", gap: 0.5, width: "100%" }}>
                <Stack direction="row" alignItems="center" spacing={1.5}>
                    <Skeleton variant="circular" width={24} height={24} />
                    <Skeleton variant="text" width="30%" height={28} />
                    <Box sx={{ flexGrow: 1 }} />
                    <Skeleton variant="rounded" width={120} height={6} sx={{ borderRadius: 3 }} />
                    <Skeleton variant="text" width={60} height={20} />
                </Stack>
                <Stack direction="row" alignItems="center" spacing={1} sx={{ pl: 4.5 }}>
                    <Skeleton variant="text" width="50%" height={20} />
                    <Box sx={{ flexGrow: 1 }} />
                    <Skeleton variant="text" width={80} height={16} />
                </Stack>
            </Box>
        </ListItem>
    );
}
