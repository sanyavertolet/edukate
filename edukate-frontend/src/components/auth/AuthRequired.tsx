import { useAuthContext } from "./AuthContextProvider";
import { ReactNode, useState } from "react";
import { Box, Link, Paper, Typography } from "@mui/material";
import { SignInComponent } from "./SignInComponent";
import { SignUpComponent } from "./SignUpComponent";

type AuthRequiredProps = {
    children: ReactNode;
    bypass?: boolean;
};

export function AuthRequired({ children, bypass = false }: AuthRequiredProps) {
  const { isAuthorized } = useAuthContext();
  const [isSignUp, setIsSignUp] = useState(false);

  if (isAuthorized || bypass) return children;

  return (
    <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
      <Paper
        elevation={3}
        sx={{
          maxWidth: 500,
          p: 3,
          display: "flex",
          flexDirection: "column",
          gap: 2,
        }}
      >
        <Typography variant="h5" align="center">
          Authentication Required
        </Typography>
        <Typography variant="body1">
          Please log in to continue.
        </Typography>

        {isSignUp ? (
          <SignUpComponent key="signup" shouldRefreshInsteadOfNavigate />
        ) : (
          <SignInComponent key="signin" shouldRefreshInsteadOfNavigate />
        )}

      {isSignUp ? (
        <Link
          component="button"
          type="button"
          onClick={() => setIsSignUp(false)}
          color="secondary"
          underline="hover"
          sx={{ alignSelf: "center" }}
        >
          Already have an account? Sign in
        </Link>
      ) : (
        <Link
          component="button"
          type="button"
          onClick={() => setIsSignUp(true)}
          color="secondary"
          underline="hover"
          sx={{ alignSelf: "center" }}
        >
          Don&apos;t have an account? Sign up
        </Link>
      )}

      </Paper>
    </Box>
  );
}
