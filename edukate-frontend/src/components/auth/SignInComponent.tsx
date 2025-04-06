import React, { useEffect, useState } from 'react';
import { Box, TextField, Button, Typography } from '@mui/material';
import { useSignInMutation } from "../../http/auth";
import { useNavigate } from "react-router-dom";

interface SignInComponentProps {
    shouldRefreshInsteadOfNavigate?: boolean;
}

export const SignInComponent = ({shouldRefreshInsteadOfNavigate = false}: SignInComponentProps) => {
    const navigate = useNavigate();
    const signInMutation = useSignInMutation();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        signInMutation.mutate({ username, password });
    };

    const onSuccess = () => { shouldRefreshInsteadOfNavigate ? window.location.reload() : navigate("/"); };
    useEffect(() => { if (signInMutation.isSuccess) { onSuccess(); } }, [signInMutation.isSuccess]);
    return (
        <Box component="form" onSubmit={ handleSubmit }>
            <TextField
                label="Username"
                type="username"
                fullWidth
                margin="normal"
                value={ username }
                onChange={ (e) => setUsername(e.target.value) }
                required
            />
            <TextField
                label="Password"
                type="password"
                fullWidth
                margin="normal"
                value={ password }
                onChange={ (e) => setPassword(e.target.value) }
                required
            />
            <Button
                variant="contained"
                color="primary"
                type="submit"
                sx={{ mt: 2 }}
                fullWidth
                disabled={ signInMutation.isPending }
            >
                { signInMutation.isPending ? 'Signing in...' : 'Sign In' }
            </Button>
            { signInMutation.isError && (
                <Typography color="error" sx={{ mt: 2 }}>
                    { signInMutation.error.message || 'Sign In failed. Please try again.' }
                </Typography>
            )}
        </Box>
    );
};
