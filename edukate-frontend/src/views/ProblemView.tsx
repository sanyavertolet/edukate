import { useParams } from "react-router-dom";

function ProblemView() {
    const { id } = useParams();

    return (
        <>
            <h1>Problem { id } </h1>
        </>
    )
}

export default ProblemView