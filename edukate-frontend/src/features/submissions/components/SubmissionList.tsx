import { useMySubmissionsQuery } from "@/features/submissions/api";
import { FC, useState } from "react";
import { Box, List } from "@mui/material";
import { ImageLightbox } from "@/shared/components/images/ImageLightbox";
import { EmptySubmissionListStub, ErrorListItem, StubListItem, SubmissionListItem } from "./SubmissionListItems";
import { Submission } from "@/features/submissions/types";

type SubmissionListProps = { problemKey?: string; onSubmissionClick?: (submission: Submission) => void };

export const SubmissionList: FC<SubmissionListProps> = ({ problemKey, onSubmissionClick }) => {
    const { data: submissions, isLoading, isError, error } = useMySubmissionsQuery(problemKey);
    const [lightbox, setLightbox] = useState<{ images: string[]; index: number } | null>(null);

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
                        />
                    ))}
            </List>
        </Box>
    );
};

function composeSubmissionKey(s: Submission) {
    return `${s.problemKey}:${s.userName}:${s.createdAt}`;
}
