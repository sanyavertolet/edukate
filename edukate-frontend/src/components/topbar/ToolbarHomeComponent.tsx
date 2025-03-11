import { Avatar, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

export const ToolbarHomeComponent = () => {
    const navigate = useNavigate();
    return (
        <Button color="primary" onClick={ () => navigate("/") }>
            <Avatar alt="Home" src="logo.png" sx={{ mr: 2 }} />
            <Typography>
                Edukate
            </Typography>
        </Button>
    );
}
