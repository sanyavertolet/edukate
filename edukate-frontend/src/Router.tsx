import { createBrowserRouter } from "react-router-dom";
import PageSkeleton from "./components/PageSkeleton";
import ProblemView from "./views/ProblemView";
import ProblemListView from "./views/ProblemListView";
import IndexView from "./views/IndexView";
import SignInView from "./views/SignInView";
import SignUpView from "./views/SignUpView";
import BundleListView from "./views/BundleListView";
import BundleView from "./views/BundleView";
import BundleCreationView from "./views/BundleCreationView";
import { AuthRequired } from "./components/auth/AuthRequired";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <PageSkeleton/>,
        children: [
            {
                path: "/",
                element: <IndexView/>,
            },
            {
                path: "/problems",
                element: <ProblemListView/>,
            },
            {
                path: "/problems/:id",
                element: <ProblemView/>,
            },
            {
                path: "/sign-in",
                element: <SignInView/>,
            },
            {
                path: "/sign-up",
                element: <SignUpView/>,
            },
            {
                path: "/bundles",
                element: (
                    <BundleListView/>
                ),
            },
            {
                path: "/bundles/new",
                element: (
                    <AuthRequired>
                        <BundleCreationView/>
                    </AuthRequired>
                ),
            },
            {
                path: "/bundles/:code",
                element: (
                    <AuthRequired>
                        <BundleView/>
                    </AuthRequired>
                ),
            },
        ],
    },
]);
