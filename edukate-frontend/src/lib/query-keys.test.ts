import { queryKeys } from "./query-keys";

describe("queryKeys", () => {
    describe("problems", () => {
        it("list includes page, size and optional filters", () => {
            expect(queryKeys.problems.list(0, 20)).toEqual([
                "problems",
                "list",
                0,
                20,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
            ]);
            expect(queryKeys.problems.list(1, 10, "1.", "SOLVED", true, false, true, "savchenko")).toEqual([
                "problems",
                "list",
                1,
                10,
                "1.",
                "SOLVED",
                true,
                false,
                true,
                "savchenko",
            ]);
        });

        it("detail includes key", () => {
            expect(queryKeys.problems.detail("savchenko/1.1.1")).toEqual(["problems", "detail", "savchenko/1.1.1"]);
        });

        it("answer includes bookSlug and code", () => {
            expect(queryKeys.problems.answer("savchenko", "1.1.1")).toEqual(["problems", "answer", "savchenko", "1.1.1"]);
        });

        it("count includes optional filters", () => {
            expect(queryKeys.problems.count()).toEqual([
                "problems",
                "count",
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
            ]);
            expect(queryKeys.problems.count("1.", "SOLVED", true, undefined, undefined, "savchenko")).toEqual([
                "problems",
                "count",
                "1.",
                "SOLVED",
                true,
                undefined,
                undefined,
                "savchenko",
            ]);
        });

        it("random is a static array", () => {
            expect(queryKeys.problems.random).toEqual(["problems", "random"]);
        });
    });

    describe("problemSets", () => {
        it("detail includes code", () => {
            expect(queryKeys.problemSets.detail("abc")).toEqual(["problemSets", "detail", "abc"]);
        });

        it("list includes category", () => {
            expect(queryKeys.problemSets.list("joined")).toEqual(["problemSets", "list", "joined"]);
        });

        it("users includes code", () => {
            expect(queryKeys.problemSets.users("abc")).toEqual(["problemSets", "users", "abc"]);
        });

        it("invitedUsers includes code", () => {
            expect(queryKeys.problemSets.invitedUsers("abc")).toEqual(["problemSets", "invited-users", "abc"]);
        });
    });

    describe("submissions", () => {
        it("detail includes id", () => {
            expect(queryKeys.submissions.detail("sub-1")).toEqual(["submissions", "detail", "sub-1"]);
        });

        it("byProblem includes problemKey", () => {
            expect(queryKeys.submissions.byProblem("savchenko/1.1.1")).toEqual([
                "submissions",
                "by-problem",
                "savchenko/1.1.1",
            ]);
        });
    });

    describe("notifications", () => {
        it("list includes isRead, page and size", () => {
            expect(queryKeys.notifications.list(true, 0, 10)).toEqual(["notifications", "list", true, 0, 10]);
        });

        it("list accepts undefined isRead", () => {
            expect(queryKeys.notifications.list(undefined, 1, 5)).toEqual(["notifications", "list", undefined, 1, 5]);
        });
    });

    describe("checks", () => {
        it("bySubmission includes id", () => {
            expect(queryKeys.checks.bySubmission("sub-42")).toEqual(["checks", "by-submission", "sub-42"]);
        });
    });

    describe("files", () => {
        it("temp is a static array", () => {
            expect(queryKeys.files.temp).toEqual(["files", "temp"]);
        });

        it("tempFile includes path", () => {
            expect(queryKeys.files.tempFile("/tmp/file.txt")).toEqual(["files", "temp", "/tmp/file.txt"]);
        });
    });

    describe("auth", () => {
        it("whoami is a static array", () => {
            expect(queryKeys.auth.whoami).toEqual(["auth", "whoami"]);
        });
    });
});
