import {Avatar, Button, Stack, Typography} from "@mui/material";
import { FC } from "react";

type SiteMarkProps = {
    onClick?: () => void;
};

export const SiteMark: FC<SiteMarkProps> = ({onClick}) => {
    const edukateTypographySx = { display: 'flex', fontWeight: 'bold', ml: 1 };

    if (onClick) {
        return (
            <Button color="primary" onClick={onClick}>
                <Avatar alt="Edukate" src="/logo.png" sx={{ width: 32, height: 32 }} />
                <Typography variant="h6" component="div" sx={edukateTypographySx}>
                    Edukate
                </Typography>
            </Button>
        );
    }

    return (
        <Stack direction={"row"}>
            <Avatar alt="Edukate" src="/logo.png" sx={{ width: 32, height: 32 }} />
            <Typography variant="h6" component="span" color={"primary"} sx={edukateTypographySx} textTransform={"uppercase"}>
                Edukate
            </Typography>
        </Stack>
    );
}
