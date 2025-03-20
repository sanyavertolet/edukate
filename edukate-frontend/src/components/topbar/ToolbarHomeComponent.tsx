import { Avatar, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

export const ToolbarHomeComponent = () => {
    const navigate = useNavigate();
    return (
        <Button color="primary" onClick={ () => navigate("/") }>
            <Avatar alt="Home" src="logo.png"/>
            <Typography sx={{ display: { xs: "none", md: "flex" }, ml: 2}}>
                Edukate
            </Typography>
        </Button>
    );
}
