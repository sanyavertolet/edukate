import { useMutation, useQuery } from "@tanstack/react-query";
import { getMySupervisorTickets, supervisorVerdict as submitVerdict, SupervisorVerdict } from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";

export function useSupervisorTicketsQuery(problemSetShareCode: string, page = 0, size = 20) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.supervisorTickets.byProblemSet(problemSetShareCode, page, size),
        enabled: isAuthorized && !!problemSetShareCode,
        queryFn: ({ signal }) => getMySupervisorTickets({ problemSetCode: problemSetShareCode, page, size }, signal),
    });
}

export function useSubmitVerdictMutation(problemSetShareCode: string, page: number, size: number) {
    return useMutation({
        mutationFn: ({ ticketId, verdict }: { ticketId: number; verdict: SupervisorVerdict }) =>
            submitVerdict(ticketId, verdict),
        onSuccess: () => {
            void queryClient.invalidateQueries({
                queryKey: queryKeys.supervisorTickets.byProblemSet(problemSetShareCode, page, size),
            });
        },
    });
}
