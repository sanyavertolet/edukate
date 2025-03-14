export interface Result {
    id: string,
    text: string,
    notes: string | null,
    type: "FORMULA" | "TEXT" | "NUMERIC",
    images: string[],
}
