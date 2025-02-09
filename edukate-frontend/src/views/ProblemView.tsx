import { useParams } from "react-router-dom";

export default function ProblemView() {
    const { id } = useParams();

    return (
        <>
            <h1>Problem { id } </h1>
        </>
    )
}
