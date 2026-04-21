import { useParams } from "react-router-dom";
import { ProblemSetComponent } from "@/features/problem-sets/components/ProblemSetComponent";

export default function ProblemSetPage() {
    const { code } = useParams();
    return <ProblemSetComponent problemSetCode={code} />;
}
