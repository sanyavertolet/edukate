import { useMySubmissionsQuery } from "../../http/requests/submissions";
import { FC, useState } from "react";
import { Box, List } from "@mui/material";
import { ZoomingImageDialog } from "../images/ZoomingImageDialog";
import { EmptySubmissionListStub, ErrorListItem, StubListItem, SubmissionListItem } from "./SubmissionListItems";
import { Submission } from "../../types/submission/Submission";

type SubmissionListComponentProps = { problemId?: string; };

export const SubmissionListComponent: FC<SubmissionListComponentProps> = ({ problemId }) => {
    const { data: submissions, isLoading, isError, error } = useMySubmissionsQuery(problemId);
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const showEmpty = !isLoading && !isError && submissions && submissions.length === 0;
    return (
        <Box>
            <ZoomingImageDialog selectedImage={selectedImage} handleClose={() => setSelectedImage(null)} />
            <List disablePadding>
                {isLoading && (
                    <>
                        {Array.from({ length: 3 }).map((_, i) => (
                            <StubListItem key={`stub-${i}`} />
                        ))}
                    </>
                )}

                {isError && <ErrorListItem error={error}/>}

                {showEmpty && <EmptySubmissionListStub />}

                {!isLoading && !isError && submissions && submissions.length > 0 &&
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
}

function composeSubmissionKey(s: Submission) {
    return `${s.problemId}:${s.userName}:${s.createdAt}`;
}
