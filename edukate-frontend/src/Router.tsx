import { createBrowserRouter } from "react-router-dom";

import PageSkeleton from "./components/PageSkeleton";
import ProblemView from "./views/ProblemView";
import ProblemListView from "./views/ProblemListView";
import IndexView from "./views/IndexView";
import SignInView from "./views/SignInView";
import SignUpView from "./views/SignUpView";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <PageSkeleton/>,
        children: [
            {
                path: "/",
                element: <IndexView/>
            },
            {
                path: "/problems",
                element: <ProblemListView />
            },
            {
                path: "/problems/:id",
                element: <ProblemView/>
            },
            {
                path: "/sign-in",
                element: <SignInView/>
            },
            {
                path: "/sign-up",
                element: <SignUpView/>
            },
        ],
    },
]);
