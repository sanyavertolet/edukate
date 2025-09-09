import React, { useEffect, useState } from 'react';
import { Box, TextField, Button, Typography } from '@mui/material';
import { useSignInMutation } from "../../http/auth";
import { useNavigate } from "react-router-dom";
import { queryClient } from "../../http/queryClient";
import { PasswordField } from './PasswordField';

interface SignInComponentProps {
  shouldRefreshInsteadOfNavigate?: boolean;
}

export const SignInComponent = ({ shouldRefreshInsteadOfNavigate = false }: SignInComponentProps) => {
  const navigate = useNavigate();
  const signInMutation = useSignInMutation();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    signInMutation.mutate({ username, password });
  };

  useEffect(() => {
    if (signInMutation.isSuccess) {
      queryClient.refetchQueries({ queryKey: ["whoami"] }).finally(() => {
        if (shouldRefreshInsteadOfNavigate) {
          window.location.reload();
        } else {
          navigate("/");
        }
      });
    }
  }, [signInMutation.isSuccess]);

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <TextField
        label="Username"
        type="text"
        autoComplete="username"
        fullWidth
        margin="normal"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        required
      />
      <PasswordField
        label="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <Button
        variant="contained"
        color="primary"
        type="submit"
        sx={{ mt: 2 }}
        fullWidth
        disabled={signInMutation.isPending}
      >
        {signInMutation.isPending ? 'Signing in...' : 'Sign In'}
      </Button>
      {signInMutation.isError && (
        <Typography color="error" sx={{ mt: 2 }}>
          {signInMutation.error.message || 'Sign In failed. Please try again.'}
        </Typography>
      )}
    </Box>
  );
};
