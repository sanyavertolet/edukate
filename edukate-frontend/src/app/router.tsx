import { createBrowserRouter } from "react-router-dom";
import { lazy } from "react";
import PageSkeleton from "@/shared/components/layout/PageSkeleton";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

const IndexPage = lazy(() => import("@/pages/IndexPage"));
const ProblemListPage = lazy(() => import("@/pages/ProblemListPage"));
const ProblemPage = lazy(() => import("@/pages/ProblemPage"));
const SignInPage = lazy(() => import("@/pages/SignInPage"));
const SignUpPage = lazy(() => import("@/pages/SignUpPage"));
const BundleListPage = lazy(() => import("@/pages/BundleListPage"));
const BundlePage = lazy(() => import("@/pages/BundlePage"));
const BundleCreationPage = lazy(() => import("@/pages/BundleCreationPage"));
const SubmissionPage = lazy(() => import("@/pages/SubmissionPage"));

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
