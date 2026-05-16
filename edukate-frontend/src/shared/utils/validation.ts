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
    if (6 > password.trim().length || password.trim().length > 128) {
        return "password_length_error";
    }
    return null;
};

const validateUsername: Validation<string> = (username: string) => {
    const trimmed = username.trim();
    if (trimmed.length < 3 || trimmed.length > 15) return "username_length_error";
    if (!/^[a-zA-Z]/.test(trimmed)) return "username_start_error";
    if (!/[a-zA-Z0-9]$/.test(trimmed)) return "username_end_error";
    if (/[^a-zA-Z0-9_-]/.test(trimmed)) return "username_chars_error";
    return null;
};
