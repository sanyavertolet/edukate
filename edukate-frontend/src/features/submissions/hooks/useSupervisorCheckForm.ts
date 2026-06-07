import { useCallback, useEffect, useRef, useState } from "react";
import { useRequestCheckMutation } from "@/features/checks/api";
import { useMemberProblemSetsSearch } from "@/features/problem-sets/api";
import { ProblemSetMetadata } from "@/features/problem-sets/types";

type UseSupervisorCheckFormParams = {
    submissionId: string;
    problemKey: string;
    defaultProblemSetCode?: string;
    onSubmitted?: () => void;
};

export type SupervisorCheckFormState = {
    problemKey: string;
    selectedProblemSet: ProblemSetMetadata | null;
    onProblemSetChange: (ps: ProblemSetMetadata | null) => void;
    supervisorName: string | null;
    setSupervisorName: (name: string | null) => void;
    isValid: boolean;
    isPending: boolean;
    handleSubmit: () => void;
};

export function useSupervisorCheckForm({
    submissionId,
    problemKey,
    defaultProblemSetCode,
    onSubmitted,
}: UseSupervisorCheckFormParams): SupervisorCheckFormState {
    const [selectedProblemSet, setSelectedProblemSet] = useState<ProblemSetMetadata | null>(null);
    const [supervisorName, setSupervisorName] = useState<string | null>(null);
    const mutation = useRequestCheckMutation();
    const { data: candidateSets } = useMemberProblemSetsSearch(problemKey);
    // One-shot seed: when a default share code is provided AND the matching set arrives in the
    // dropdown's data, pre-select it. We intentionally use useEffect to copy data into local
    // state — the field is user-mutable after the seed, so true derived state would prevent
    // the user from clearing or changing the auto-pick.
    const hasSeeded = useRef(false);

    useEffect(() => {
        if (hasSeeded.current || !defaultProblemSetCode || selectedProblemSet !== null) return;
        const match = candidateSets.find((s) => s.shareCode === defaultProblemSetCode);
        if (match) {
            setSelectedProblemSet(match);
            hasSeeded.current = true;
        }
    }, [defaultProblemSetCode, candidateSets, selectedProblemSet]);

    const onProblemSetChange = useCallback((ps: ProblemSetMetadata | null) => {
        setSelectedProblemSet(ps);
        setSupervisorName(null);
    }, []);

    const isValid = selectedProblemSet !== null && supervisorName !== null;
    const pendingType = mutation.variables?.checkType;
    const isPending = mutation.isPending && pendingType === "supervisor";

    const handleSubmit = useCallback(() => {
        if (!isValid) return;
        mutation.mutate(
            {
                checkType: "supervisor",
                submissionId,
                problemKey,
                problemSetCode: selectedProblemSet.shareCode,
                supervisorName,
            },
            { onSuccess: () => onSubmitted?.() },
        );
    }, [isValid, mutation, submissionId, problemKey, selectedProblemSet, supervisorName, onSubmitted]);

    return {
        problemKey,
        selectedProblemSet,
        onProblemSetChange,
        supervisorName,
        setSupervisorName,
        isValid,
        isPending,
        handleSubmit,
    };
}
