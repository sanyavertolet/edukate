import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import enCommon from "./locales/en/common.json";
import enAuth from "./locales/en/auth.json";
import enNavigation from "./locales/en/navigation.json";
import enProblems from "./locales/en/problems.json";
import enChecks from "./locales/en/checks.json";
import enSubmissions from "./locales/en/submissions.json";
import enProblemSets from "./locales/en/problem-sets.json";

import ruCommon from "./locales/ru/common.json";
import ruAuth from "./locales/ru/auth.json";
import ruNavigation from "./locales/ru/navigation.json";
import ruProblems from "./locales/ru/problems.json";
import ruChecks from "./locales/ru/checks.json";
import ruSubmissions from "./locales/ru/submissions.json";
import ruProblemSets from "./locales/ru/problem-sets.json";

void i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            en: {
                common: enCommon,
                auth: enAuth,
                navigation: enNavigation,
                problems: enProblems,
                checks: enChecks,
                submissions: enSubmissions,
                "problem-sets": enProblemSets,
            },
            ru: {
                common: ruCommon,
                auth: ruAuth,
                navigation: ruNavigation,
                problems: ruProblems,
                checks: ruChecks,
                submissions: ruSubmissions,
                "problem-sets": ruProblemSets,
            },
        },
        fallbackLng: "en",
        defaultNS: "common",
        ns: ["common", "auth", "navigation", "problems", "checks", "submissions", "problem-sets"],
        interpolation: {
            escapeValue: false,
        },
        detection: {
            order: ["localStorage", "navigator"],
            caches: ["localStorage"],
        },
    });

export default i18n;
