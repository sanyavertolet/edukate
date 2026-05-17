import {
    Alert,
    Box,
    Button,
    Card,
    CardActions,
    CardContent,
    CircularProgress,
    Container,
    FormControlLabel,
    Stack,
    Step,
    StepLabel,
    Stepper,
    Switch,
    TextField,
    Typography,
} from "@mui/material";
import { getApiErrorMessage } from "@/lib/api-error";
import { useEffect, useState } from "react";
import { OptionPickerComponent } from "@/shared/components/OptionPicker";
import { CreateProblemSetRequest } from "@/features/problem-sets/types";
import { useCreateProblemSetMutation } from "@/features/problem-sets/api";
import { inviteToProblemSet } from "@/generated/backend";
import { ProblemSetProblemPicker } from "@/features/problem-sets/components/ProblemSetProblemPicker";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function ProblemSetCreationPage() {
    const { t } = useTranslation("problem-sets");
    const [activeStep, setActiveStep] = useState(0);
    const [createProblemSetRequest, setCreateProblemSetRequest] = useState<CreateProblemSetRequest>({
        name: "",
        description: "",
        isPublic: false,
        problemKeys: [],
    });
    const [inviteUsernames, setInviteUsernames] = useState<string[]>([]);
    const [failedInvites, setFailedInvites] = useState<string[]>([]);
    const [createdShareCode, setCreatedShareCode] = useState<string | null>(null);

    const problemSetMutation = useCreateProblemSetMutation(createProblemSetRequest);
    const navigate = useNavigate();

    useEffect(() => {
        if (problemSetMutation.isSuccess) {
            const shareCode = problemSetMutation.data.shareCode;
            if (inviteUsernames.length === 0) {
                void navigate(`/problem-sets/${shareCode}`);
                return;
            }
            void Promise.allSettled(
                inviteUsernames.map((username) => inviteToProblemSet(shareCode, { inviteeName: username })),
            ).then((results) => {
                const failed = inviteUsernames.filter((_, i) => results[i].status === "rejected");
                if (failed.length > 0) {
                    setFailedInvites(failed);
                    setCreatedShareCode(shareCode);
                } else {
                    void navigate(`/problem-sets/${shareCode}`);
                }
            });
        }
    }, [problemSetMutation.isSuccess, problemSetMutation.data, navigate, inviteUsernames]);

    const isStep0Valid = createProblemSetRequest.name.trim() !== "" && createProblemSetRequest.description.trim() !== "";
    const isCreateEnabled =
        !problemSetMutation.isPending &&
        !problemSetMutation.isSuccess &&
        isStep0Valid &&
        createProblemSetRequest.problemKeys.length > 0;

    const steps = [t("stepper_step_details"), t("stepper_step_problems"), t("stepper_step_invite_users")];

    const stepContent: Record<number, React.ReactNode> = {
        0: (
            <Stack spacing={2}>
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
                <FormControlLabel
                    control={
                        <Switch
                            checked={createProblemSetRequest.isPublic}
                            onChange={(e) => {
                                setCreateProblemSetRequest({ ...createProblemSetRequest, isPublic: e.target.checked });
                            }}
                        />
                    }
                    label={t("visibility_toggle_label")}
                />
            </Stack>
        ),
        1: (
            <ProblemSetProblemPicker
                selectedKeys={createProblemSetRequest.problemKeys}
                onSelectionChange={(problemKeys) => {
                    setCreateProblemSetRequest({ ...createProblemSetRequest, problemKeys });
                }}
            />
        ),
        2: (
            <OptionPickerComponent
                optionsUrl="/api/v1/users/by-prefix"
                selectedOptions={inviteUsernames}
                label={t("stepper_step_invite_users")}
                placeholderText={t("search_users_placeholder")}
                onOptionsChange={setInviteUsernames}
            />
        ),
    };

    return (
        <Container maxWidth="md" sx={{ px: { xs: 0, md: 2 } }}>
            <Card>
                <CardContent>
                    <Stack spacing={3}>
                        <Typography component="h1" color="secondary" variant="h5">
                            {t("create_problem_set_title")}
                        </Typography>
                        <Stepper activeStep={activeStep}>
                            {steps.map((label) => (
                                <Step key={label}>
                                    <StepLabel>{label}</StepLabel>
                                </Step>
                            ))}
                        </Stepper>
                        <Box>{stepContent[activeStep]}</Box>
                    </Stack>
                </CardContent>
                <CardActions sx={{ flexDirection: "column", gap: 1 }}>
                    <Stack direction="row" spacing={1} sx={{ mx: "auto" }}>
                        {activeStep > 0 && (
                            <Button
                                variant="outlined"
                                onClick={() => {
                                    setActiveStep((s) => s - 1);
                                }}
                            >
                                {t("stepper_back_button")}
                            </Button>
                        )}
                        {activeStep < steps.length - 1 && (
                            <Button
                                variant="outlined"
                                disabled={activeStep === 0 && !isStep0Valid}
                                onClick={() => {
                                    setActiveStep((s) => s + 1);
                                }}
                            >
                                {t("stepper_next_button")}
                            </Button>
                        )}
                        {activeStep >= 1 && (
                            <Button
                                variant="contained"
                                disabled={!isCreateEnabled}
                                startIcon={problemSetMutation.isPending ? <CircularProgress size={20} /> : undefined}
                                onClick={() => {
                                    problemSetMutation.mutate();
                                }}
                            >
                                {t("create_problem_set_button")}
                            </Button>
                        )}
                    </Stack>
                    {problemSetMutation.isError && (
                        <Alert severity="error" sx={{ width: "100%" }}>
                            {getApiErrorMessage(problemSetMutation.error)}
                        </Alert>
                    )}
                    {failedInvites.length > 0 && createdShareCode && (
                        <Alert
                            severity="warning"
                            sx={{ width: "100%" }}
                            action={
                                <Button
                                    color="inherit"
                                    size="small"
                                    onClick={() => {
                                        void navigate(`/problem-sets/${createdShareCode}`);
                                    }}
                                >
                                    {t("go_to_problem_set_button")}
                                </Button>
                            }
                        >
                            {t("invite_failed_message", { users: failedInvites.join(", ") })}
                        </Alert>
                    )}
                </CardActions>
            </Card>
        </Container>
    );
}
