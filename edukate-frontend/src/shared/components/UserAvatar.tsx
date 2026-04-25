import { Avatar, SxProps, Theme } from "@mui/material";
import { getColorByStringHash, getFirstLetters } from "@/shared/utils/utils";
import { FC } from "react";

interface UserAvatarProps {
    name: string;
    size?: "small" | "medium";
    highlighted?: boolean;
}

const sizeMap = {
    small: { width: 28, height: 28, fontSize: 12 },
    medium: { width: 32, height: 32, fontSize: 14 },
};

export const UserAvatar: FC<UserAvatarProps> = ({ name, size = "medium", highlighted = false }) => {
    const dimensions = sizeMap[size];
    const sx: SxProps<Theme> = {
        ...dimensions,
        backgroundColor: getColorByStringHash(name),
        ...(highlighted && {
            outline: "2px solid",
            outlineColor: "primary.main",
            outlineOffset: 2,
        }),
    };

    return <Avatar sx={sx}>{getFirstLetters(name, 2)}</Avatar>;
};
