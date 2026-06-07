import { FC } from "react";
import { Stack } from "@mui/material";
import { SupervisorCheckFormState } from "@/features/submissions/hooks/useSupervisorCheckForm";
import { ProblemSetSelector } from "./ProblemSetSelector";
import { SupervisorAutocomplete } from "./SupervisorAutocomplete";

export const SupervisorCheckFormFields: FC<{ form: SupervisorCheckFormState }> = ({ form }) => (
    <Stack spacing={2}>
        <ProblemSetSelector
            value={form.selectedProblemSet}
            onChange={form.onProblemSetChange}
            problemKey={form.problemKey}
        />
        <SupervisorAutocomplete
            problemSetShareCode={form.selectedProblemSet?.shareCode ?? null}
            value={form.supervisorName}
            onChange={form.setSupervisorName}
        />
    </Stack>
);
