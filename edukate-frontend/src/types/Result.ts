export interface Result {
    id: string,
    text: string,
    notes: string | null,
    type: ResultType,
    images: string[],
}

export type ResultType = "FORMULA" | "TEXT" | "NUMERIC";
