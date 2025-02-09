import './App.css'
import router from "./Router.tsx";
import {RouterProvider} from "react-router-dom";

export default function App() {
    return (
        <RouterProvider router={router} />
    )
}
