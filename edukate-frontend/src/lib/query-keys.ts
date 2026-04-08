export const queryKeys = {
    problems: {
        all: ["problems"] as const,
        list: (page: number, size: number) => ["problems", "list", page, size] as const,
        detail: (id: string) => ["problems", "detail", id] as const,
        count: ["problems", "count"] as const,
        random: ["problems", "random"] as const,
        result: (id: string) => ["problems", "result", id] as const,
    },
    bundles: {
        all: ["bundles"] as const,
        detail: (code: string) => ["bundles", "detail", code] as const,
        list: (category: string) => ["bundles", "list", category] as const,
        users: (code: string) => ["bundles", "users", code] as const,
        invitedUsers: (code: string) => ["bundles", "invited-users", code] as const,
    },
    submissions: {
        all: ["submissions"] as const,
        detail: (id: string) => ["submissions", "detail", id] as const,
        byProblem: (problemId: string) => ["submissions", "by-problem", problemId] as const,
    },
    notifications: {
        all: ["notifications"] as const,
        // isRead/page/size are part of the key so each combination is cached independently
        list: (isRead: boolean | undefined, page: number, size: number) =>
            ["notifications", "list", isRead, page, size] as const,
        stats: ["notifications", "stats"] as const,
    },
    checks: {
        all: ["checks"] as const,
        bySubmission: (id: string) => ["checks", "by-submission", id] as const,
    },
    files: {
        all: ["files"] as const,
        temp: ["files", "temp"] as const,
        tempFile: (path: string) => ["files", "temp", path] as const,
    },
    auth: {
        whoami: ["auth", "whoami"] as const,
    },
};
