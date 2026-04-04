import { lazy, Suspense } from "react";

const LatexComponent = lazy(() => import("./LatexComponent"));

interface LazyLatexComponentProps {
    text: string;
}

export function LazyLatexComponent({ text }: LazyLatexComponentProps) {
    return (
        <Suspense fallback={<span>{text}</span>}>
            <LatexComponent text={text} />
        </Suspense>
    );
}
