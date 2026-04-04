import { useBundleRequest } from "@/features/bundles/api";
import { useState } from "react";
import { Box, Card } from "@mui/material";
import { BundleProblemSelector } from "./BundleProblemSelector";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { BundleIndexCard } from "./BundleIndexCard";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "@/shared/context/DeviceContext";

interface BundleComponentProps {
    bundleCode?: string;
}

export function BundleComponent({ bundleCode }: BundleComponentProps) {
    const { isMobile } = useDeviceContext();
    const { data: bundle } = useBundleRequest(bundleCode);
    const [selectedProblemMetadata, setSelectedProblemMetadata] = useState<ProblemMetadata>();
    const onProblemSelect = (problemOrUndefined?: ProblemMetadata) => {
        setSelectedProblemMetadata(problemOrUndefined);
    };
    return (
        <Box>
            <Grid container spacing={1} paddingTop={"1rem"}>
                <Grid sx={{ sm: "none", md: "block" }} key={"left-grid"} size={"grow"}>
                    <Card>
                        <BundleProblemSelector
                            bundleName={bundle ? bundle.name : "Index"}
                            problems={bundle ? bundle.problems : []}
                            onProblemSelect={onProblemSelect}
                            selectedProblem={selectedProblemMetadata}
                        />
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={isMobile ? 12 : 10}>
                    {selectedProblemMetadata ? (
                        <ProblemComponent problemId={selectedProblemMetadata.name} />
                    ) : (
                        <BundleIndexCard bundle={bundle} />
                    )}
                </Grid>
            </Grid>
        </Box>
    );
}
