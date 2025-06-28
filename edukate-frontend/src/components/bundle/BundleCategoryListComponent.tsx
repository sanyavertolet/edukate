import { Box, Card, CardContent, Container, Snackbar, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleCard } from "./BundleCard";
import { useBundlesRequest } from "../../http/requests";
import { useEffect, useState } from "react";
import { BundleMetadata } from "../../types/bundle/BundleMetadata";
import { BundleCategory } from "../../types/bundle/Bundle";

interface BundleCategoryListComponentProps {
    tab: BundleCategory;
}

export function BundleCategoryListComponent({tab}: BundleCategoryListComponentProps) {
    const { data, isLoading, error } = useBundlesRequest(tab);
    const [bundles, setBundles] = useState<BundleMetadata[]>([]);
    useEffect(() => { if (data && !isLoading && !error) { setBundles(data); } }, [data, isLoading, error, tab]);

    const [open, setOpen] = useState(false);
    const onSnackbarClose = () => setOpen(false);
    return (
        <Box>
            <Grid container rowSpacing={ 2 } spacing={ 2 }>

                { bundles.length == 0 && <BundleEmptyListComponent/>}

                { bundles.map((bundle, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3}} key={`grid-${index}`}>
                        <BundleCard bundleMetadata={bundle} onCopy={() => setOpen(true)}/>
                    </Grid>
                ))}
            </Grid>

            <Snackbar open={open} autoHideDuration={1000} onClose={onSnackbarClose} message={"Copied to clipboard."}/>
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
