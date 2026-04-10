import { useMySubmissionsQuery } from "@/features/submissions/api";
import { FC, useState } from "react";
import { Box, List } from "@mui/material";
import { ZoomingImageDialog } from "@/shared/components/images/ZoomingImageDialog";
import { EmptySubmissionListStub, ErrorListItem, StubListItem, SubmissionListItem } from "./SubmissionListItems";
import { Submission } from "@/features/submissions/types";

type SubmissionListProps = { problemId?: string };

export const SubmissionList: FC<SubmissionListProps> = ({ problemId }) => {
    const { data: submissions, isLoading, isError, error } = useMySubmissionsQuery(problemId);
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const showEmpty = !isLoading && !isError && submissions && submissions.length === 0;
    return (
        <Box>
            <ZoomingImageDialog selectedImage={selectedImage} handleClose={() => { setSelectedImage(null); }} />
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
                            setImage={setSelectedImage}
                        />
                    ))}
            </List>
        </Box>
    );
};

function composeSubmissionKey(s: Submission) {
    return `${s.problemId}:${s.userName}:${s.createdAt}`;
}
