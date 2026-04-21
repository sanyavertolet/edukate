import { AxiosError, AxiosHeaders } from "axios";
import { getApiErrorMessage } from "./api-error";

function makeAxiosError(data: Record<string, unknown> | undefined, status?: number): AxiosError {
    const err = new AxiosError("request failed");
    err.response = {
        data,
        status: status ?? 500,
        statusText: "Internal Server Error",
        headers: {},
        config: { headers: new AxiosHeaders() },
    };
    return err;
}

describe("getApiErrorMessage", () => {
    it("returns data.message when the axios response contains a message field", () => {
        const err = makeAxiosError({ message: "Validation failed", error: "Bad Request" });
        expect(getApiErrorMessage(err)).toBe("Validation failed");
    });

    it("falls back to friendly status message when server message is absent", () => {
        const err = makeAxiosError({ error: "Unauthorized" }, 401);
        expect(getApiErrorMessage(err)).toBe("Authentication required");
    });

    it("falls back to friendly status message when server message exceeds max length", () => {
        const longMessage = "a".repeat(81);
        const err = makeAxiosError({ message: longMessage }, 403);
        expect(getApiErrorMessage(err)).toBe("Access denied");
    });

    it("falls back to generic error string for unknown status codes", () => {
        const err = makeAxiosError({}, 418);
        expect(getApiErrorMessage(err)).toBe("Error 418");
    });

    it("returns 'Something went wrong' when response is missing entirely", () => {
        const err = new AxiosError("network failure");
        // err.response is undefined — no status to map
        expect(getApiErrorMessage(err)).toBe("Something went wrong");
    });

    it("returns error.message for a plain Error instance", () => {
        expect(getApiErrorMessage(new Error("plain error"))).toBe("plain error");
    });

    it("returns 'Something went wrong' for unknown non-Error values", () => {
        expect(getApiErrorMessage("string error")).toBe("Something went wrong");
        expect(getApiErrorMessage(42)).toBe("Something went wrong");
        expect(getApiErrorMessage(null)).toBe("Something went wrong");
    });
});
