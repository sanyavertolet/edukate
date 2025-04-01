import { Bundle } from "../../types/Bundle";
import { Card, CardContent, Container, Typography } from "@mui/material";

interface BundleInfoCardComponentProps {
    bundle?: Bundle;
}

export function BundleInfoCardComponent({bundle}: BundleInfoCardComponentProps) {
    return (
        <Card>
            <CardContent>
                <Container>
                    <Typography variant={"body1"}>
                        { bundle?.description }
                    </Typography>
                </Container>
            </CardContent>
        </Card>
    )
}
