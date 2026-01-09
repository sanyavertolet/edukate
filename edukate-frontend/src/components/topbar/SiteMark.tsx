import { Avatar, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { FC } from "react";

type SiteMarkProps = {
    onClick?: () => void;
};

export const SiteMark: FC<SiteMarkProps> = ({onClick}) => {
    const navigate = useNavigate();
    const edukateTypographySx = {
        display: 'flex',
        fontWeight: 'bold',
        ml: 1,
    };

    return (
        <Button color="primary" onClick={onClick ?? (() => navigate("/"))}>
            <Avatar alt="Edukate" src="/logo.png" sx={{ width: 32, height: 32 }} />
            <Typography variant="h6" component="div" sx={edukateTypographySx}>
                Edukate
            </Typography>
        </Button>
    );
}
