import React, { useEffect, useState } from 'react';
import { Box, TextField, Button, Typography } from '@mui/material';
import { useSignUpMutation } from "../../http/auth";
import { useNavigate } from "react-router-dom";
import { validate } from "../../utils/validation";
import { queryClient } from "../../http/queryClient";
import { PasswordField } from './PasswordField';

interface FormDataItem {
  value: string;
  error: string | null;
}

interface SignUpFormData {
  username: FormDataItem;
  email: FormDataItem;
  password: FormDataItem;
}

const initialFormData: SignUpFormData = {
  username: { value: '', error: null },
  email: { value: '', error: null },
  password: { value: '', error: null },
};

interface SignUpComponentProps {
  shouldRefreshInsteadOfNavigate?: boolean;
}

export const SignUpComponent = ({ shouldRefreshInsteadOfNavigate = false }: SignUpComponentProps) => {
  const [formData, setFormData] = useState<SignUpFormData>(initialFormData);
  const navigate = useNavigate();
  const signUpMutation = useSignUpMutation();

  const updateField = (field: keyof SignUpFormData, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: { value, error: value ? validate(field, value) : null }
    }));
  };

  const isFormReady = (): boolean => {
    return !!formData.email.error || !!formData.password.error || !!formData.username.error ||
      !formData.username.value.length || !formData.email.value.length || !formData.password.value.length;
  };

  const validateForm = (): boolean => {
    const newFormData = { ...formData };
    let isValid = true;

    const fields: (keyof SignUpFormData)[] = ['username', 'email', 'password'];
    fields.forEach(field => {
      const value = formData[field].value;
      const error = validate(field, value);
      if (error) {
        isValid = false;
        newFormData[field].error = error;
      }
    });

    setFormData(newFormData);
    return isValid;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      signUpMutation.mutate({
        username: formData.username.value,
        email: formData.email.value,
        password: formData.password.value
      });
    }
  };

  useEffect(() => {
    if (signUpMutation.isSuccess) {
      queryClient.refetchQueries({ queryKey: ["whoami"] }).finally(() => {
        if (shouldRefreshInsteadOfNavigate) {
          window.location.reload();
        } else {
          navigate("/");
        }
      });
    }
  }, [signUpMutation.isSuccess]);

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <TextField
        label="Username"
        type="text"
        margin="normal"
        value={formData.username.value}
        error={!!formData.username.error}
        helperText={formData.username.error}
        onChange={(e) => updateField('username', e.target.value)}
        inputMode="text"
        autoComplete="username"
        fullWidth
        required
      />
      <TextField
        label="Email"
        type="email"
        margin="normal"
        value={formData.email.value}
        error={!!formData.email.error}
        helperText={formData.email.error}
        onChange={(e) => updateField('email', e.target.value)}
        inputMode="email"
        autoComplete="email"
        fullWidth
        required
      />
      <PasswordField
        label="Password"
        value={formData.password.value}
        error={!!formData.password.error}
        helperText={formData.password.error}
        onChange={(e) => updateField('password', e.target.value)}
        required
      />
      <Button
        type="submit"
        variant="contained"
        color="primary"
        fullWidth
        sx={{ mt: 2 }}
        disabled={isFormReady() || signUpMutation.isPending}
      >
        {signUpMutation.isPending ? 'Signing up...' : 'Sign Up'}
      </Button>
      {signUpMutation.isError && (
        <Typography color="error" sx={{ mt: 2 }}>
          {signUpMutation.error.message || 'Sign Up failed. Please try again.'}
        </Typography>
      )}
    </Box>
  );
};
