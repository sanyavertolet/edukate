import { createBrowserRouter } from "react-router-dom";

import ProblemView from "./views/ProblemView";
import ProblemListView from "./views/ProblemListView";
import IndexView from "./views/IndexView";
import PageSkeleton from "./components/PageSkeleton";

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