import {
    Alert,
    Button,
    Card,
    CardActions,
    CardContent,
    CircularProgress,
    Container,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import { getApiErrorMessage } from "@/lib/api-error";
import { useEffect, useMemo, useState } from "react";
import { OptionPickerComponent } from "@/shared/components/OptionPicker";
import { ClearableTextField } from "@/shared/components/ClearableTextField";
import { CreateProblemSetRequest } from "@/features/problem-sets/types";
import { useCreateProblemSetMutation } from "@/features/problem-sets/api";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function ProblemSetCreationPage() {
    const { t } = useTranslation("problem-sets");
    const [createProblemSetRequest, setCreateProblemSetRequest] = useState<CreateProblemSetRequest>({
        name: "",
        description: "",
        isPublic: false,
        problemKeys: [],
    });
    const [bookSlugFilter, setBookSlugFilter] = useState("");
    const extraParams = useMemo(() => (bookSlugFilter ? { bookSlugPrefix: bookSlugFilter } : undefined), [bookSlugFilter]);

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
                            {t("create_problem_set_title")}
                        </Typography>
                        <TextField
                            required
                            label={t("title_label")}
                            value={createProblemSetRequest.name}
                            onChange={(e) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, name: e.target.value });
                            }}
                        />
                        <TextField
                            required
                            label={t("description_label")}
                            value={createProblemSetRequest.description}
                            onChange={(e) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, description: e.target.value });
                            }}
                        />
                        <ClearableTextField
                            label={t("book_label")}
                            value={bookSlugFilter}
                            size="medium"
                            onChange={setBookSlugFilter}
                            onClear={() => {
                                setBookSlugFilter("");
                            }}
                        />
                        <OptionPickerComponent
                            optionsUrl={"/api/v1/problems/by-prefix"}
                            selectedOptions={createProblemSetRequest.problemKeys}
                            label={t("problems_label")}
                            placeholderText={t("search_problems_placeholder")}
                            debounceTime={500}
                            extraParams={extraParams}
                            onOptionsChange={(problemKeys) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, problemKeys: problemKeys });
                            }}
                        />
                    </Stack>
                </CardContent>

                <CardActions sx={{ flexDirection: "column", gap: 1 }}>
                    <Button
                        variant={"contained"}
                        sx={{ mx: "auto" }}
                        disabled={problemSetMutation.isPending}
                        startIcon={problemSetMutation.isPending ? <CircularProgress size={20} /> : undefined}
                        onClick={() => {
                            createProblemSet();
                        }}
                    >
                        {t("create_problem_set_button")}
                    </Button>
                    {problemSetMutation.isError && (
                        <Alert severity="error" sx={{ width: "100%" }}>
                            {getApiErrorMessage(problemSetMutation.error)}
                        </Alert>
                    )}
                </CardActions>
            </Card>
        </Container>
    );
}
