import { RouterProvider } from "react-router-dom";
import { Providers } from "./providers";
import { router } from "./router";
import { ToastContainer, Zoom } from "react-toastify";

export default function App() {
    return (
        <Providers>
            <RouterProvider router={router} />
            <ToastContainer
                aria-label={"Edukate toasts"}
                position="bottom-left"
                autoClose={5000}
                hideProgressBar
                newestOnTop={false}
                closeOnClick={false}
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="colored"
                transition={Zoom}
                style={{ color: "primary" }}
            />
        </Providers>
    );
}
