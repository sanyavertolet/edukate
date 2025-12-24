import { Button, Card, CardActions, CardContent, Container, Stack, TextField, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { OptionPickerComponent } from "../components/basic/OptionPickerComponent";
import { CreateBundleRequest } from "../types/bundle/CreateBundleRequest";
import { useCreateBundleMutation } from "../http/requests/bundles";
import { useNavigate } from "react-router-dom";

export default function BundleCreationView() {

    const [createBundleRequest, setCreateBundleRequest] = useState<CreateBundleRequest>({
        name: "",
        description: "",
        isPublic: false,
        problemIds: []
    })

    const bundleMutation = useCreateBundleMutation(createBundleRequest)

    const navigate = useNavigate();
    useEffect(() => {
        if (bundleMutation.isSuccess && bundleMutation.data) {
            navigate(`/bundles/${bundleMutation.data.shareCode}`);
        }
    }, [bundleMutation.isSuccess, bundleMutation.data, bundleMutation.error]);

    const createBundle = () => { bundleMutation.mutate() };

    return (
        <Container maxWidth={"md"}>
            <Card>
                <CardContent>
                    <Stack spacing={2}>
                        <Typography color={"secondary"} variant={"h5"}>Create Bundle</Typography>
                        <TextField
                            required
                            label={"Title"}
                            value={createBundleRequest.name}
                            onChange={e =>
                                setCreateBundleRequest({...createBundleRequest, name: e.target.value})}
                        />
                        <TextField
                            required
                            label={"Description"}
                            value={createBundleRequest.description}
                            onChange={e =>
                                setCreateBundleRequest({...createBundleRequest, description: e.target.value})}
                        />
                        <OptionPickerComponent
                            optionsUrl={"/api/v1/problems/by-prefix"}
                            selectedOptions={createBundleRequest.problemIds}
                            label={"Problems"}
                            placeholderText={"Search problems"}
                            debounceTime={1000}
                            onOptionsChange={problemIds =>
                                setCreateBundleRequest({...createBundleRequest, problemIds: problemIds})}
                        />
                    </Stack>
                </CardContent>

                <CardActions>
                    <Button variant={"contained"} sx={{ mx: "auto" }} onClick={() => createBundle()}>Create Bundle</Button>
                {/*  todo: display errors  */}
                </CardActions>
            </Card>
        </Container>
    );
}
