import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./app/App";

// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- standard React entry point
createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <App />
    </StrictMode>,
);
