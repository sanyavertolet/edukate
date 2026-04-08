import { MutationCache, QueryCache, QueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { getApiErrorMessage } from "./api-error";

export const queryClient = new QueryClient({
    queryCache: new QueryCache({
        onError: (error, query) => {
            if (query.meta?.silent) return;
            toast.error(getApiErrorMessage(error));
        },
    }),
    mutationCache: new MutationCache({
        onError: (error, _variables, _context, mutation) => {
            if (mutation.meta?.silent) return;
            toast.error(getApiErrorMessage(error));
        },
    }),
    defaultOptions: {
        queries: {
            staleTime: 60_000,
            retry: 1,
        },
    },
});
