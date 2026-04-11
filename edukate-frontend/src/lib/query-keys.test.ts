import { queryKeys } from "./query-keys";

describe("queryKeys", () => {
    describe("problems", () => {
        it("list includes page, size and optional filters", () => {
            expect(queryKeys.problems.list(0, 20)).toEqual(["problems", "list", 0, 20, undefined, undefined, undefined, undefined, undefined]);
            expect(queryKeys.problems.list(1, 10, "1.", "SOLVED", true, false, true)).toEqual(["problems", "list", 1, 10, "1.", "SOLVED", true, false, true]);
        });

        it("detail includes id", () => {
            expect(queryKeys.problems.detail("prob-1")).toEqual(["problems", "detail", "prob-1"]);
        });

        it("result includes id", () => {
            expect(queryKeys.problems.result("prob-1")).toEqual(["problems", "result", "prob-1"]);
        });

        it("count includes optional filters", () => {
            expect(queryKeys.problems.count()).toEqual(["problems", "count", undefined, undefined, undefined, undefined, undefined]);
            expect(queryKeys.problems.count("1.", "SOLVED", true)).toEqual(["problems", "count", "1.", "SOLVED", true, undefined, undefined]);
        });

        it("random is a static array", () => {
            expect(queryKeys.problems.random).toEqual(["problems", "random"]);
        });
    });

    describe("bundles", () => {
        it("detail includes code", () => {
            expect(queryKeys.bundles.detail("abc")).toEqual(["bundles", "detail", "abc"]);
        });

        it("list includes category", () => {
            expect(queryKeys.bundles.list("joined")).toEqual(["bundles", "list", "joined"]);
        });

        it("users includes code", () => {
            expect(queryKeys.bundles.users("abc")).toEqual(["bundles", "users", "abc"]);
        });

        it("invitedUsers includes code", () => {
            expect(queryKeys.bundles.invitedUsers("abc")).toEqual(["bundles", "invited-users", "abc"]);
        });
    });

    describe("submissions", () => {
        it("detail includes id", () => {
            expect(queryKeys.submissions.detail("sub-1")).toEqual(["submissions", "detail", "sub-1"]);
        });

        it("byProblem includes problemId", () => {
            expect(queryKeys.submissions.byProblem("prob-1")).toEqual(["submissions", "by-problem", "prob-1"]);
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
