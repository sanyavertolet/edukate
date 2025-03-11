type Validation<Type> = (value: Type) => string | null;

export const validate = (field: "email" | "username" | "password", value: string) => {
    switch (field) {
        case 'username': return validateUsername(value);
        case 'email': return  validateEmail(value);
        case 'password': return  validatePassword(value);
    }
};

const validateEmail: Validation<string> = (email: string) => {
    if (!email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        return "Invalid email address";
    }
    return null;
};

const validatePassword: Validation<string> = (password: string) => {
    if (6 > password.trim().length || password.trim().length > 20) {
        return "Password must be between 6 and 20 characters long";
    }
    return null;
};

const validateUsername: Validation<string> = (username: string) => {
    if (3 > username.trim().length || username.trim().length > 15) {
        return "Username must be between 3 and 15 characters long";
    }
    if (!username.match(/^[a-zA-Z][a-zA-Z0-9_-]+[a-zA-Z0-9]$/)) {
        return "Username can only contain letters, numbers, underscores, and hyphens AND must start with a letter and end with a letter or number";
    }
    return null;
};
