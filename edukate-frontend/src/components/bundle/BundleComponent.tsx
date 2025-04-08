import { useBundleRequest } from "../../http/requests";
import { useEffect, useState } from "react";
import { Bundle } from "../../types/Bundle";
import { Box, Card } from "@mui/material";
import { BundleProblemSelectorComponent } from "./BundleProblemSelectorComponent";
import { ProblemMetadata } from "../../types/ProblemMetadata";
import { ProblemComponent } from "../problem/ProblemComponent";
import { BundleInfoCardComponent } from "./BundleInfoCardComponent";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "../topbar/DeviceContextProvider";

interface BundleComponentProps {
    bundleCode?: string;
    onLoaded?: (bundle: Bundle) => void;
}

export function BundleComponent({ bundleCode, onLoaded }: BundleComponentProps) {
    const [bundle, setBundle] = useState<Bundle>();
    const { data, isLoading, error } = useBundleRequest(bundleCode);
    const { isMobile } = useDeviceContext();

    useEffect(() => {
        if (data && !isLoading && !error) {
            setBundle(data);
            onLoaded && onLoaded(data);
        }
    }, [data, isLoading, error]);


    const [selectedProblemMetadata, setSelectedProblemMetadata] = useState<ProblemMetadata>();
    const onProblemSelect = (problemOrUndefined?: ProblemMetadata) => {
        setSelectedProblemMetadata(problemOrUndefined)
    };

    return (
        <Box>
            <Grid container spacing={ 1 } paddingTop={"1rem"}>
                <Grid sx={{sm: "none", md: "block"}} key={"left-grid"} size={ "grow" }>
                    <Card>
                        <BundleProblemSelectorComponent bundleName={ bundle ? bundle.name : "Index" }
                                                        problems={ bundle ? bundle.problems : [] }
                                                        onProblemSelect={onProblemSelect}
                                                        selectedProblem={selectedProblemMetadata}/>
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={ isMobile ? 12 : 10 }>
                    { selectedProblemMetadata
                        ? <ProblemComponent problemId={selectedProblemMetadata.name}/>
                        : <BundleInfoCardComponent bundle={bundle}/>
                    }
                </Grid>
            </Grid>
        </Box>
    );
}
