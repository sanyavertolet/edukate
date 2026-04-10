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

    it("falls back to data.error when message is absent", () => {
        const err = makeAxiosError({ error: "Unauthorized" });
        expect(getApiErrorMessage(err)).toBe("Unauthorized");
    });

    it("falls back to status code string when both message and error are absent", () => {
        const err = makeAxiosError({}, 403);
        expect(getApiErrorMessage(err)).toBe("403 error");
    });

    it("returns 'Unknown error' when response is missing entirely", () => {
        const err = new AxiosError("network failure");
        // err.response is undefined — status falls back to "Unknown"
        expect(getApiErrorMessage(err)).toBe("Unknown error");
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
