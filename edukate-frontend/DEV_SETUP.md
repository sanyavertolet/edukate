# Developer Setup Guide

---

## 1. Node.js

The project pins Node **22 LTS** in `.nvmrc`.

### Install a Node version manager

Use **fnm** (fast, cross-platform, works on macOS/Linux/Windows):

```bash
# macOS (Homebrew)
brew install fnm

# Add to your shell profile (~/.zshrc or ~/.bashrc):
eval "$(fnm env --use-on-cd)"
```

Or use **nvm** (macOS/Linux only):

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
```

### Switch to the correct Node version

```bash
cd edukate-frontend
fnm use        # reads .nvmrc automatically
# or:
nvm use        # same
```

### IntelliJ IDEA

Go to **Settings → Languages & Frameworks → Node.js** and point the Node interpreter to
the fnm/nvm-managed binary (e.g. `~/.fnm/node-versions/v22.x.x/installation/bin/node`).

---

## 2. Install dependencies

```bash
cd edukate-frontend
npm install
```

---

## 3. Formatter: Prettier

Config lives in `.prettierrc.json`. Key settings:

| Setting         | Value   | Reason                                                              |
| --------------- | ------- | ------------------------------------------------------------------- |
| `semi`          | `true`  | Existing codebase uses semicolons                                   |
| `singleQuote`   | `false` | Consistent with JSX double-quote convention                         |
| `trailingComma` | `"all"` | Cleaner diffs — adding a last item doesn't change the previous line |
| `printWidth`    | `100`   | Comfortable on split-screen without being too permissive            |
| `tabWidth`      | `4`     | Matches existing code style                                         |

### Scripts

```bash
npm run format          # auto-format all files in src/
npm run format:check    # check without writing (used in CI)
```

Add to `package.json` scripts:

```json
"format":       "prettier --write src",
"format:check": "prettier --check src"
```

### IntelliJ IDEA integration

1. Install the **Prettier** plugin (bundled in IntelliJ since 2022.1 — go to
   **Settings → Plugins** and confirm it's enabled).
2. Go to **Settings → Languages & Frameworks → JavaScript → Prettier**.
3. Set **Prettier package** to `edukate-frontend/node_modules/prettier`.
4. Enable **Run on save** and **Run on 'Reformat Code'**.

After this, ⌘⌥L (Reformat Code) and save both trigger Prettier automatically.

---

## 4. Linter: ESLint

```bash
npm run lint          # check for violations
npm run lint:fix      # auto-fix what ESLint can fix
npm run lint:sarif    # emit eslint-results.sarif (used by CI to upload to GitHub Code Scanning)
```

Add to `package.json` scripts:

```json
"lint:fix":  "eslint . --fix",
"lint:sarif": "eslint . --format @microsoft/eslint-formatter-sarif --output-file eslint-results.sarif"
```

`lint:sarif` requires the formatter package:

```bash
npm install --save-dev @microsoft/eslint-formatter-sarif
```

Add `eslint-results.sarif` to `.gitignore` — it's a build artefact.

### IntelliJ IDEA integration

ESLint is auto-detected by IntelliJ when `eslint` is in `node_modules`. If not:

1. **Settings → Languages & Frameworks → JavaScript → Code Quality Tools → ESLint**
2. Set to **Automatic ESLint configuration**
3. Enable **Run eslint --fix on save**

---

## 5. Type checker

TypeScript type checking is part of `npm run build` (via `tsc -b`). Add a standalone
script for faster feedback without building:

```json
"typecheck": "tsc -b"
```

```bash
npm run typecheck     # fast type-check without emitting files
```

---

## 6. Pre-commit hooks: Husky + lint-staged

Runs Prettier + ESLint automatically on every `git commit`, only on staged files.
This means you can never accidentally commit unformatted or lint-failing code.

### Install

```bash
npm install --save-dev husky lint-staged
npx husky init
```

This creates `.husky/pre-commit`. Replace its contents with:

```sh
npx lint-staged
```

### `lint-staged` config — add to `package.json`

```json
"lint-staged": {
  "src/**/*.{ts,tsx}": [
    "prettier --write",
    "eslint --fix --max-warnings 0"
  ],
  "src/**/*.{json,css,html}": [
    "prettier --write"
  ]
}
```

`--max-warnings 0` means any ESLint warning blocks the commit, not just errors.

### Verify

```bash
git add src/utils/validation.ts
git commit -m "test hooks"
# → Prettier runs, then ESLint runs. If either fails, commit is blocked.
```

---

## 7. VS Code (optional — for contributors not using IntelliJ)

The `.gitignore` allows `.vscode/extensions.json`. Create it to suggest extensions
to contributors who open the project in VS Code:

```json
{
    "recommendations": ["esbenp.prettier-vscode", "dbaeumer.vscode-eslint", "bradlc.vscode-tailwindcss"]
}
```

And `.vscode/settings.json` (also allowed by `.gitignore`):

```json
{
    "editor.formatOnSave": true,
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.codeActionsOnSave": {
        "source.fixAll.eslint": "explicit"
    }
}
```

---

## 8. Full local dev workflow

```bash
# Terminal 1 — infrastructure
docker compose up -d

# Terminal 2 — gateway
./gradlew :edukate-gateway:bootRun --args='--spring.profiles.active=dev,secure'

# Terminal 3 — backend
./gradlew :edukate-backend:bootRun --args='--spring.profiles.active=dev,secure,local,notifier'

# Terminal 4 — frontend
cd edukate-frontend
fnm use
npm install
npm run dev          # Vite HMR at http://localhost
```

### Regenerate API types (after backend model changes)

```bash
# Requires gateway running
npm run generate:types
```

---

## 9. Before opening a PR — local checklist

```bash
npm run typecheck    # zero type errors
npm run lint         # zero ESLint errors/warnings
npm run format:check # zero Prettier violations
npm run build        # production build succeeds
```

The pre-commit hook covers format + lint automatically. Run `typecheck` and `build`
manually before pushing — CI will catch them anyway, but faster feedback locally.
