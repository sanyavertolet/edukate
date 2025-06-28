import { useBundleRequest } from "../../http/requests";
import { useState } from "react";
import { Box, Card } from "@mui/material";
import { BundleProblemSelectorComponent } from "./BundleProblemSelectorComponent";
import { ProblemMetadata } from "../../types/problem/ProblemMetadata";
import { ProblemComponent } from "../problem/ProblemComponent";
import { BundleIndexCardComponent } from "./BundleIndexCardComponent";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "../topbar/DeviceContextProvider";

interface BundleComponentProps {
    bundleCode?: string;
}

export function BundleComponent({ bundleCode }: BundleComponentProps) {
    const { isMobile } = useDeviceContext();
    const { data: bundle } = useBundleRequest(bundleCode);
    const [selectedProblemMetadata, setSelectedProblemMetadata] = useState<ProblemMetadata>();
    const onProblemSelect = (problemOrUndefined?: ProblemMetadata) => {
        setSelectedProblemMetadata(problemOrUndefined)
    };
    return (
        <Box>
            <Grid container spacing={ 1 } paddingTop={"1rem"}>
                <Grid sx={{sm: "none", md: "block"}} key={"left-grid"} size={ "grow" }>
                    <Card>
                        <BundleProblemSelectorComponent
                            bundleName={ bundle ? bundle.name : "Index" }
                            problems={ bundle ? bundle.problems : [] }
                            onProblemSelect={onProblemSelect}
                            selectedProblem={selectedProblemMetadata}
                        />
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={ isMobile ? 12 : 10 }>
                    { selectedProblemMetadata
                        ? <ProblemComponent problemId={selectedProblemMetadata.name}/>
                        : <BundleIndexCardComponent bundle={bundle}/>
                    }
                </Grid>
            </Grid>
        </Box>
    );
}
