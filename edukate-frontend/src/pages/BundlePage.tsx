import { useParams } from "react-router-dom";
import { BundleComponent } from "@/features/bundles/components/BundleComponent";

export default function BundlePage() {
    const { code } = useParams();
    return <BundleComponent bundleCode={code} />;
}
