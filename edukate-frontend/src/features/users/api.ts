import { useQuery } from "@tanstack/react-query";
import { getUserInfo } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";

const FIVE_MINUTES_MS = 5 * 60 * 1000;

/**
 * Batch-fetch user info (name + avatar URL) for a list of usernames.
 *
 * Sorted + deduplicated names form the query key so the same set in different
 * orders shares a single cache entry. Returns a `name → UserInfoDto` map;
 * unknown names are omitted by the backend.
 *
 * Use anywhere a list of users needs avatars without rewriting the upstream
 * DTO (problem-set members, submission authors, notification senders, etc.).
 */
export function useUserInfoMap(names: string[]) {
    const sortedNames = [...new Set(names)].sort();
    return useQuery({
        queryKey: queryKeys.users.infoMap(sortedNames),
        queryFn: ({ signal }) => getUserInfo({ names: sortedNames }, signal),
        enabled: sortedNames.length > 0,
        staleTime: FIVE_MINUTES_MS,
    });
}
