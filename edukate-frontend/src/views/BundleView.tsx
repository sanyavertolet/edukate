import { useParams } from "react-router-dom";
import { BundleComponent } from "../components/bundle/BundleComponent";

export default function BundleView() {
    const { code } = useParams();
    return ( <BundleComponent bundleCode={ code }/> );
}
