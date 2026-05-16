import { createBrowserRouter } from "react-router-dom";
import { lazy } from "react";
import PageSkeleton from "@/shared/components/layout/PageSkeleton";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

const IndexPage = lazy(() => import("@/pages/IndexPage"));
const ProblemListPage = lazy(() => import("@/pages/ProblemListPage"));
const ProblemPage = lazy(() => import("@/pages/ProblemPage"));
const SignInPage = lazy(() => import("@/pages/SignInPage"));
const SignUpPage = lazy(() => import("@/pages/SignUpPage"));
const ForgotPasswordPage = lazy(() => import("@/pages/ForgotPasswordPage"));
const ResetPasswordPage = lazy(() => import("@/pages/ResetPasswordPage"));
const ProblemSetListPage = lazy(() => import("@/pages/ProblemSetListPage"));
const ProblemSetPage = lazy(() => import("@/pages/ProblemSetPage"));
const ProblemSetCreationPage = lazy(() => import("@/pages/ProblemSetCreationPage"));
const SubmissionPage = lazy(() => import("@/pages/SubmissionPage"));
const SubmissionListPage = lazy(() => import("@/pages/SubmissionListPage"));

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
                path: "/problems/:bookSlug/:code",
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
                path: "/forgot-password",
                element: <ForgotPasswordPage />,
            },
            {
                path: "/reset-password",
                element: <ResetPasswordPage />,
            },
            {
                path: "/problem-sets",
                element: <ProblemSetListPage />,
            },
            {
                path: "/problem-sets/new",
                element: (
                    <AuthRequired>
                        <ProblemSetCreationPage />
                    </AuthRequired>
                ),
            },
            {
                path: "/problem-sets/:code",
                element: <ProblemSetPage />,
            },
            {
                path: "/submissions",
                element: (
                    <AuthRequired>
                        <SubmissionListPage />
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
