import React, { useEffect, useState } from 'react';
import { Box, TextField, Button, Typography } from '@mui/material';
import { useSignUpMutation } from "../../http/auth";
import { useNavigate } from "react-router-dom";

export const SignUpComponent = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const signUpMutation = useSignUpMutation();

    useEffect(() => { if (signUpMutation.isSuccess) { navigate("/"); } }, [signUpMutation.isSuccess]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        signUpMutation.mutate({ username, password, email });
    };

    return (
        <Box component="form" onSubmit={ handleSubmit }>
            <TextField
                label="Username"
                type="username"
                margin="normal"
                value={ username }
                onChange={ (e) => setUsername(e.target.value) }
                fullWidth
                required
            />
            <TextField
                label="Email"
                type="email"
                margin="normal"
                value={ email }
                onChange={ (e) => setEmail(e.target.value) }
                fullWidth
                required
            />
            <TextField
                label="Password"
                type="password"
                margin="normal"
                value={ password }
                onChange={ (e) => setPassword(e.target.value) }
                fullWidth
                required
            />
            <Button
                type="submit"
                variant="contained"
                color="primary"
                fullWidth
                sx={{ mt: 2 }}
                loading={ signUpMutation.isPending }
            >
                { signUpMutation.isPending ? 'Signing up...' : 'Sign Up' }
            </Button>
            { signUpMutation.isError && (
                <Typography color="error" sx={{ mt: 2 }}>
                    { signUpMutation.error.message || 'Sign Up failed. Please try again.' }
                </Typography>
            )}
        </Box>
    );
};
