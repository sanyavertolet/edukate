export type PasswordStrengthScore = 0 | 1 | 2 | 3 | 4;

export interface PasswordStrength {
    /** 0 = empty (no bar). 1 = weak … 4 = strong. */
    score: PasswordStrengthScore;
    /** i18n key under the `settings` namespace, or null when empty. */
    labelKey: string | null;
}

const CHARACTER_CLASSES = [/[a-z]/, /[A-Z]/, /\d/, /[^A-Za-z0-9]/];

const LABEL_KEYS = ["pw_strength_weak", "pw_strength_fair", "pw_strength_good", "pw_strength_strong"];

/**
 * Lightweight, dependency-free strength estimate. Rewards length and character-class
 * variety; deliberately conservative so a bare-minimum 6-char password reads as "weak".
 * This is UX guidance only — the authoritative rule is the 6–128 length check in `validate`.
 */
export function passwordStrength(password: string): PasswordStrength {
    if (!password) return { score: 0, labelKey: null };

    const variety = CHARACTER_CLASSES.filter((pattern) => pattern.test(password)).length;

    let score = 1;
    if (password.length >= 8) score += 1;
    if (password.length >= 12) score += 1;
    if (variety >= 3) score += 1;

    const clamped = Math.min(4, score) as PasswordStrengthScore;
    return { score: clamped, labelKey: LABEL_KEYS[clamped - 1] };
}
