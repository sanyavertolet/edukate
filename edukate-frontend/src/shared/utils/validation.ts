type Validation<Type> = (value: Type) => string | null;

type ValidationTarget = "email" | "username" | "password";

export const validate = (field: ValidationTarget, value: string) => {
    switch (field) {
        case "username":
            return validateUsername(value);
        case "email":
            return validateEmail(value);
        case "password":
            return validatePassword(value);
    }
};

const validateEmail: Validation<string> = (email: string) => {
    if (!email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        return "email_invalid_error";
    }
    return null;
};

const validatePassword: Validation<string> = (password: string) => {
    if (6 > password.trim().length || password.trim().length > 20) {
        return "password_length_error";
    }
    return null;
};

const validateUsername: Validation<string> = (username: string) => {
    if (3 > username.trim().length || username.trim().length > 15) {
        return "username_length_error";
    }
    if (!username.match(/^[a-zA-Z][a-zA-Z0-9_-]+[a-zA-Z0-9]$/)) {
        return "username_format_error";
    }
    return null;
};
