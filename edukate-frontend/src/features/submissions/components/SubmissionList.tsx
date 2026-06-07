import { useMySubmissionsQuery } from "@/features/submissions/api";
import { FC, useState } from "react";
import { Box, List } from "@mui/material";
import { useSearchParams } from "react-router-dom";
import { ImageLightbox } from "@/shared/components/images/ImageLightbox";
import { useRequestCheckMutation } from "@/features/checks/api";
import { useAuthContext } from "@/features/auth/context";
import { Role } from "@/features/auth/types";
import {
    EmptySubmissionListStub,
    ErrorListItem,
    RowCheckType,
    StubListItem,
    SubmissionListItem,
} from "./SubmissionListItems";
import { SupervisorCheckDialog } from "./SupervisorCheckDialog";
import { Submission } from "@/features/submissions/types";

type SubmissionListProps = {
    problemKey?: string;
    problemSetCode?: string;
    onSubmissionClick?: (submission: Submission) => void;
};

export const SubmissionList: FC<SubmissionListProps> = ({ problemKey, problemSetCode, onSubmissionClick }) => {
    const { data: submissions, isLoading, isError, error } = useMySubmissionsQuery(problemKey);
    const [lightbox, setLightbox] = useState<{ images: string[]; index: number } | null>(null);
    const [selectedSupervisorSubmission, setSelectedSupervisorSubmission] = useState<Submission | null>(null);
    const [searchParams] = useSearchParams();
    const effectiveProblemSetCode = problemSetCode ?? searchParams.get("problemSet") ?? undefined;

    const { user } = useAuthContext();
    const isAiCheckDisabled = !(["MODERATOR", "ADMIN"] as Role[]).some((role) => user?.roles.includes(role));

    const requestCheckMutation = useRequestCheckMutation();
    const pendingVars = requestCheckMutation.isPending ? requestCheckMutation.variables : undefined;
    const pendingSubmissionId = pendingVars && pendingVars.checkType !== "supervisor" ? pendingVars.submissionId : null;
    const pendingCheckType: RowCheckType | null =
        pendingVars && pendingVars.checkType !== "supervisor" ? pendingVars.checkType : null;

    const handleSelfCheck = (submission: Submission) => {
        requestCheckMutation.mutate({
            checkType: "self",
            submissionId: String(submission.id),
            problemKey: submission.problemKey,
        });
    };
    const handleSmartCheck = (submission: Submission) => {
        requestCheckMutation.mutate({
            checkType: "ai",
            submissionId: String(submission.id),
            problemKey: submission.problemKey,
        });
    };
    const handleSupervisorCheck = (submission: Submission) => {
        setSelectedSupervisorSubmission(submission);
    };

    const showEmpty = !isLoading && !isError && submissions && submissions.length === 0;
    return (
        <Box>
            {lightbox && (
                <ImageLightbox
                    images={lightbox.images}
                    index={lightbox.index}
                    open
                    onClose={() => {
                        setLightbox(null);
                    }}
                />
            )}
            <SupervisorCheckDialog
                submission={selectedSupervisorSubmission}
                defaultProblemSetCode={effectiveProblemSetCode}
                onClose={() => {
                    setSelectedSupervisorSubmission(null);
                }}
            />
            <List disablePadding>
                {isLoading && (
                    <>
                        {Array.from({ length: 3 }).map((_, i) => (
                            <StubListItem key={`stub-${String(i)}`} />
                        ))}
                    </>
                )}

                {isError && <ErrorListItem error={error} />}

                {showEmpty && <EmptySubmissionListStub />}

                {!isLoading &&
                    !isError &&
                    submissions &&
                    submissions.length > 0 &&
                    submissions.map((submission) => (
                        <SubmissionListItem
                            key={composeSubmissionKey(submission)}
                            submission={submission}
                            openImages={(images, index) => {
                                setLightbox({ images, index });
                            }}
                            onSelect={onSubmissionClick}
                            onSelfCheck={handleSelfCheck}
                            onSmartCheck={handleSmartCheck}
                            onSupervisorCheck={handleSupervisorCheck}
                            isAiCheckDisabled={isAiCheckDisabled}
                            pendingCheckType={pendingSubmissionId === String(submission.id) ? pendingCheckType : null}
                            isAnyCheckPending={requestCheckMutation.isPending}
                        />
                    ))}
            </List>
        </Box>
    );
};

function composeSubmissionKey(s: Submission) {
    return `${s.problemKey}:${s.userName}:${s.createdAt}`;
}
