import katex from "katex";
import { useMemo } from "react";

interface LatexComponentProps {
    text: string;
}

function renderText(text: string) {
    const result: string[] = [];

    text.split("$").forEach((fragment, index) => {
        if (index % 2 === 0) {
            result.push(fragment);
        } else {
            try {
                result.push(katex.renderToString(fragment));
            } catch (error) {
                result.push(fragment);
            }
        }
    })
    return result.join("");
}

export function LatexComponent({text}: LatexComponentProps) {
    const renderedText = useMemo(() => renderText(text), [text]);

    return (
        <span className="__Latex__" dangerouslySetInnerHTML={{ __html: renderedText }} />
    );
}
