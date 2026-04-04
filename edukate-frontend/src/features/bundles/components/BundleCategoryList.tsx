import { Box, Card, CardContent, Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleCard } from "./BundleCard";
import { useBundlesRequest } from "@/features/bundles/api";
import { BundleCategory } from "@/features/bundles/types";
import { toast } from "react-toastify";

interface BundleCategoryListProps {
    tab: BundleCategory;
}

export function BundleCategoryList({ tab }: BundleCategoryListProps) {
    const { data: bundles } = useBundlesRequest(tab);

    return (
        <Box>
            <Grid container rowSpacing={2} spacing={2}>
                {(!bundles || bundles.length == 0) && <BundleEmptyList />}

                {bundles?.map((bundle, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3 }} key={`grid-${index}`}>
                        <BundleCard bundleMetadata={bundle} onCopy={() => toast.info("Copied to clipboard.")} />
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
}

export function BundleEmptyList() {
    return (
        <Container>
            <Card>
                <CardContent>
                    <Typography variant={"h4"} color={"primary"} align={"center"}>
                        No bundles found.
                    </Typography>
                    <Typography variant={"body1"}>Create your own bundle or join one of the public bundles.</Typography>
                </CardContent>
            </Card>
        </Container>
    );
}
