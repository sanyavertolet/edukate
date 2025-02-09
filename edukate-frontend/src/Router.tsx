import { createBrowserRouter } from "react-router-dom";

import ProblemView from "./views/ProblemView.tsx";
import ProblemListView from "./views/ProblemListView.tsx";
import IndexView from "./views/IndexView.tsx";
import PageSkeleton from "./components/PageSkeleton.tsx";

const router = createBrowserRouter([
    {
        path: '/',
        element: <PageSkeleton/>,
        children: [
            {
                path: '/',
                element: <IndexView />
            },
            {
                path: '/problems',
                element: <ProblemListView />
            },
            {
                path: '/problems/:id',
                element: <ProblemView />
            }
        ]
    },
])

export default router;