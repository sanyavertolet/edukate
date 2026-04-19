import { Button, Card, CardActions, CardContent, Container, Stack, TextField, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { OptionPickerComponent } from "@/shared/components/OptionPicker";
import { CreateProblemSetRequest } from "@/features/problem-sets/types";
import { useCreateProblemSetMutation } from "@/features/problem-sets/api";
import { useNavigate } from "react-router-dom";

export default function ProblemSetCreationPage() {
    const [createProblemSetRequest, setCreateProblemSetRequest] = useState<CreateProblemSetRequest>({
        name: "",
        description: "",
        isPublic: false,
        problemKeys: [],
    });

    const problemSetMutation = useCreateProblemSetMutation(createProblemSetRequest);

    const navigate = useNavigate();
    useEffect(() => {
        if (problemSetMutation.isSuccess) {
            void navigate(`/problem-sets/${problemSetMutation.data.shareCode}`);
        }
    }, [problemSetMutation.isSuccess, problemSetMutation.data, problemSetMutation.error, navigate]);

    const createProblemSet = () => {
        problemSetMutation.mutate();
    };

    return (
        <Container maxWidth={"md"}>
            <Card>
                <CardContent>
                    <Stack spacing={2}>
                        <Typography component="h1" color={"secondary"} variant={"h5"}>
                            Create Problem Set
                        </Typography>
                        <TextField
                            required
                            label={"Title"}
                            value={createProblemSetRequest.name}
                            onChange={(e) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, name: e.target.value });
                            }}
                        />
                        <TextField
                            required
                            label={"Description"}
                            value={createProblemSetRequest.description}
                            onChange={(e) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, description: e.target.value });
                            }}
                        />
                        <OptionPickerComponent
                            optionsUrl={"/api/v1/problems/by-prefix"}
                            selectedOptions={createProblemSetRequest.problemKeys}
                            label={"Problems"}
                            placeholderText={"Search problems"}
                            debounceTime={1000}
                            onOptionsChange={(problemKeys) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, problemKeys: problemKeys });
                            }}
                        />
                    </Stack>
                </CardContent>

                <CardActions>
                    <Button
                        variant={"contained"}
                        sx={{ mx: "auto" }}
                        onClick={() => {
                            createProblemSet();
                        }}
                    >
                        Create Problem Set
                    </Button>
                    {/*  todo: display errors  */}
                </CardActions>
            </Card>
        </Container>
    );
}
