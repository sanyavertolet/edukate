import { Divider, List, Paper } from "@mui/material";
import { ProblemSetListItem } from "./ProblemSetListItem";
import { ProblemSetListItemSkeleton } from "./ProblemSetListItemSkeleton";
import { ProblemSetEmptyState } from "./ProblemSetEmptyState";
import { useProblemSetsRequest } from "@/features/problem-sets/api";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { Fragment } from "react";

interface ProblemSetCategoryListProps {
    tab: ProblemSetCategory;
    onTabSwitch?: (tab: ProblemSetCategory) => void;
}

const SKELETON_COUNT = 5;

export function ProblemSetCategoryList({ tab, onTabSwitch }: ProblemSetCategoryListProps) {
    const { data: problemSets, isPending } = useProblemSetsRequest(tab);

    if (isPending) {
        return (
            <Paper variant="outlined">
                <List disablePadding>
                    {Array.from({ length: SKELETON_COUNT }, (_, i) => (
                        <Fragment key={`skeleton-${String(i)}`}>
                            {i > 0 && <Divider />}
                            <ProblemSetListItemSkeleton />
                        </Fragment>
                    ))}
                </List>
            </Paper>
        );
    }

    if (!problemSets || problemSets.length === 0) {
        return <ProblemSetEmptyState tab={tab} onTabSwitch={onTabSwitch} />;
    }

    return (
        <Paper variant="outlined">
            <List disablePadding>
                {problemSets.map((problemSet, index) => (
                    <Fragment key={problemSet.shareCode}>
                        {index > 0 && <Divider />}
                        <ProblemSetListItem problemSetMetadata={problemSet} />
                    </Fragment>
                ))}
            </List>
        </Paper>
    );
}
