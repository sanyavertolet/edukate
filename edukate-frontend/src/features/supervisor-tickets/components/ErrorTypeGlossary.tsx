import { FC } from "react";
import { Dialog, DialogContent, DialogTitle, Divider, IconButton, List, ListItem, ListItemText } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";
import { SupervisorVerdictErrorType } from "@/generated/backend";

const ERROR_TYPE_KEYS = Object.values(SupervisorVerdictErrorType);

type ErrorTypeGlossaryProps = {
    open: boolean;
    onClose: () => void;
};

export const ErrorTypeGlossary: FC<ErrorTypeGlossaryProps> = ({ open, onClose }) => {
    const { t } = useTranslation("supervisor-tickets");
    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                {t("glossary_title")}
                <IconButton onClick={onClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ p: 0 }}>
                <List disablePadding>
                    {ERROR_TYPE_KEYS.map((key) => (
                        <ListItem key={key} divider>
                            <ListItemText
                                primary={t(`error_type_${key.toLowerCase()}`)}
                                secondary={t(`error_type_${key.toLowerCase()}_desc`)}
                            />
                        </ListItem>
                    ))}
                </List>
            </DialogContent>
        </Dialog>
    );
};
