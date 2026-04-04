import { createBrowserRouter } from "react-router-dom";
import PageSkeleton from "@/shared/components/layout/PageSkeleton";
import ProblemPage from "@/pages/ProblemPage";
import ProblemListPage from "@/pages/ProblemListPage";
import IndexPage from "@/pages/IndexPage";
import SignInPage from "@/pages/SignInPage";
import SignUpPage from "@/pages/SignUpPage";
import BundleListPage from "@/pages/BundleListPage";
import BundlePage from "@/pages/BundlePage";
import BundleCreationPage from "@/pages/BundleCreationPage";
import { AuthRequired } from "@/features/auth/components/AuthRequired";
import { SubmissionPage } from "@/pages/SubmissionPage";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <PageSkeleton />,
        children: [
            {
                path: "/",
                element: <IndexPage />,
            },
            {
                path: "/problems",
                element: <ProblemListPage />,
            },
            {
                path: "/problems/:id",
                element: <ProblemPage />,
            },
            {
                path: "/sign-in",
                element: <SignInPage />,
            },
            {
                path: "/sign-up",
                element: <SignUpPage />,
            },
            {
                path: "/bundles",
                element: <BundleListPage />,
            },
            {
                path: "/bundles/new",
                element: (
                    <AuthRequired>
                        <BundleCreationPage />
                    </AuthRequired>
                ),
            },
            {
                path: "/bundles/:code",
                element: (
                    <AuthRequired>
                        <BundlePage />
                    </AuthRequired>
                ),
            },
            {
                path: "/submissions/:id",
                element: (
                    <AuthRequired>
                        <SubmissionPage />
                    </AuthRequired>
                ),
            },
        ],
    },
]);
