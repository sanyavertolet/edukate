export const queryKeys = {
    problems: {
        all: ["problems"] as const,
        list: (
            page: number,
            size: number,
            prefix?: string,
            status?: string,
            isHard?: boolean,
            hasPictures?: boolean,
            hasResult?: boolean,
            bookSlug?: string,
        ) => ["problems", "list", page, size, prefix, status, isHard, hasPictures, hasResult, bookSlug] as const,
        detail: (key: string) => ["problems", "detail", key] as const,
        count: (
            prefix?: string,
            status?: string,
            isHard?: boolean,
            hasPictures?: boolean,
            hasResult?: boolean,
            bookSlug?: string,
        ) => ["problems", "count", prefix, status, isHard, hasPictures, hasResult, bookSlug] as const,
        random: ["problems", "random"] as const,
        answer: (bookSlug: string, code: string) => ["problems", "answer", bookSlug, code] as const,
    },
    problemSets: {
        all: ["problemSets"] as const,
        detail: (code: string) => ["problemSets", "detail", code] as const,
        list: (category: string) => ["problemSets", "list", category] as const,
        users: (code: string) => ["problemSets", "users", code] as const,
        invitedUsers: (code: string) => ["problemSets", "invited-users", code] as const,
    },
    submissions: {
        all: ["submissions"] as const,
        detail: (id: string) => ["submissions", "detail", id] as const,
        byProblem: (problemKey: string) => ["submissions", "by-problem", problemKey] as const,
    },
    notifications: {
        all: ["notifications"] as const,
        // isRead/page/size are part of the key so each combination is cached independently
        list: (isRead: boolean | undefined, page: number, size: number) =>
            ["notifications", "list", isRead, page, size] as const,
    },
    checks: {
        all: ["checks"] as const,
        bySubmission: (id: string) => ["checks", "by-submission", id] as const,
        detail: (id: string) => ["checks", "detail", id] as const,
    },
    files: {
        all: ["files"] as const,
        temp: ["files", "temp"] as const,
        tempFile: (path: string) => ["files", "temp", path] as const,
    },
    users: {
        byPrefix: ["/api/v1/users/by-prefix"] as const,
    },
    auth: {
        whoami: ["auth", "whoami"] as const,
    },
};
