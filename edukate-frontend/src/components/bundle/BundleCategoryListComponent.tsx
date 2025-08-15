import { Box, Card, CardContent, Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleCard } from "./BundleCard";
import { useBundlesRequest } from "../../http/requests";
import { useEffect, useState } from "react";
import { BundleMetadata } from "../../types/bundle/BundleMetadata";
import { BundleCategory } from "../../types/bundle/Bundle";
import { toast } from "react-toastify";

interface BundleCategoryListComponentProps {
    tab: BundleCategory;
}

export function BundleCategoryListComponent({tab}: BundleCategoryListComponentProps) {
    const { data, isLoading, error } = useBundlesRequest(tab);
    const [bundles, setBundles] = useState<BundleMetadata[]>([]);
    useEffect(() => { if (data && !isLoading && !error) { setBundles(data); } }, [data, isLoading, error, tab]);

    return (
        <Box>
            <Grid container rowSpacing={ 2 } spacing={ 2 }>

                { bundles.length == 0 && <BundleEmptyListComponent/>}

                { bundles.map((bundle, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3}} key={`grid-${index}`}>
                        <BundleCard bundleMetadata={bundle} onCopy={() => toast.info("Copied to clipboard.")}/>
                    </Grid>
                ))}
            </Grid>
        </Box>
    )
}

export function BundleEmptyListComponent() {
    return (
        <Container>
            <Card>
                <CardContent>
                    <Typography variant={"h4"} color={"primary"} align={"center"}>
                        No bundles found.
                    </Typography>
                    <Typography variant={"body1"}>
                        Create your own bundle or join one of the public bundles.
                    </Typography>
                </CardContent>
            </Card>
        </Container>
    )
}
