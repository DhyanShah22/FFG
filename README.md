# Psychometric Assessment Platform

Monorepo for the Antarang Foundation's Psychometric Assessment project (Career Assessment Platform — CAP).

---

## Table of Contents

1. [Repository structure](#repository-structure)
2. [Prerequisites](#prerequisites)
3. [Cloning the repository](#cloning-the-repository)
4. [Using a separate GitHub account (NGO vs personal)](#using-a-separate-github-account-ngo-vs-personal)
5. [Backend — local development](#backend--local-development)
6. [Database migrations (Flyway)](#database-migrations-flyway)
7. [Branching strategy](#branching-strategy)
8. [Day-to-day workflow](#day-to-day-workflow)
9. [Git hooks (path names)](#git-hooks-path-names)
10. [Commit message convention](#commit-message-convention)
11. [Pull request guidelines](#pull-request-guidelines)
12. [Branch protection rules](#branch-protection-rules)
13. [Quick command reference](#quick-command-reference)
14. [Documentation & change tracking](#documentation--change-tracking)

---

## Repository structure

```text
psychometric-assessment/
├── backend/                 # Spring Boot API (Java 21) — code + migrations only
│   └── src/main/resources/db/migration/   # Flyway V1–V16 (canonical)
├── frontend/                # UI (future)
├── docs/                    # API testing guides, IF2AF0203 PDFs (repo-level)
├── .opsx/                   # Change proposals and task tracking (repo-level)
└── flyway.conf              # Flyway CLI config (AWS / manual CLI runs)
```

Repo-level `docs/` and `.opsx/` live at the monorepo root only. Do not duplicate them under `backend/`.

See also [backend/README.md](backend/README.md) for a backend-only quick start.

---

## Prerequisites

**Everyone**

- Git ([Download Git](https://git-scm.com/downloads))
- A GitHub account with access to the `Antarang-Foundation` organization
  - Ask an admin to add you as a collaborator if you don't have access yet
- (Recommended) SSH key set up with GitHub — [SSH setup guide](https://docs.github.com/en/authentication/connecting-to-github-with-ssh)

**Backend developers**

- Java 21 (JDK)
- PostgreSQL (local dev; e.g. Postgres.app)
- Optional: [Flyway CLI](https://documentation.red-gate.com/fd) for AWS/manual migration runs (Spring Boot also runs Flyway on startup)

---

## Cloning the repository

**Over HTTPS:**

```bash
git clone https://github.com/Antarang-Foundation/psychometric-assessment.git
cd psychometric-assessment
```

**Over SSH (recommended):**

```bash
git clone git@github.com:Antarang-Foundation/psychometric-assessment.git
cd psychometric-assessment
```

If prompted for credentials over HTTPS, log in with the GitHub account that has access to this org. If using SSH, make sure your public key is added under **GitHub → Settings → SSH Keys** on the account with access.

**After clone — enable shared git hooks (once per machine):**

```bash
./scripts/setup-git-hooks.sh
```

This blocks commits with Windows-incompatible path names (e.g. folder names with trailing spaces).

---

## Using a separate GitHub account (NGO vs personal)

If you use a different GitHub account for personal projects and need to keep that separate from your NGO account, you don't need to log out/in every time you switch. Use Git Credential Manager to log out of the personal account's cached credentials for this repo, then log back in with the NGO account:

1. Check which accounts are currently cached:
   ```bash
   git credential-manager github list
   ```
2. Log out of the personal account that's currently cached:
   ```bash
   git credential-manager github logout <your-personal-username>
   ```
3. Clone (or push to) the repo — this will trigger a fresh login prompt:
   ```bash
   git clone https://github.com/Antarang-Foundation/psychometric-assessment.git
   cd psychometric-assessment
   git push -u origin main
   ```
4. When the browser/login prompt appears, sign in with the **NGO-linked GitHub account** (the one with access to `Antarang-Foundation`).

Git Credential Manager will now use the NGO account's credentials for this repo going forward. If you switch back to working on a personal repo afterward and hit the same issue in reverse, repeat the logout step for the NGO account and log back in with your personal one.

> **Note:** Since credentials are cached per Git Credential Manager session rather than strictly per-repo, if you frequently switch between personal and NGO repos, consider the SSH host-aliasing approach instead (separate SSH keys mapped to separate `Host` aliases in `~/.ssh/config`) to avoid repeated logins. Ask a maintainer if you'd like this documented as an alternative.

---

## Backend — local development

### Configuration

Default datasource in `backend/src/main/resources/application.yaml`:

| Setting | Default (local) |
|---------|-----------------|
| URL | `jdbc:postgresql://localhost:5432/antarang_cap` |
| User | your OS user (e.g. `avinashjha11`) |
| Password | empty (Postgres.app / trust auth) |
| Flyway | `classpath:db/migration` (same SQL files as CLI) |
| JPA | `ddl-auto: validate` |

Override via environment variables or a local profile as needed. Do not commit secrets.

Optional app settings: `JWT_SECRET`, `GOOGLE_CLIENT_ID` (see `application.yaml`).

### Run the API

```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS; adjust on Linux/Windows
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`

### Tests

```bash
cd backend
./mvnw clean test
```

### Optional smoke script

Signup → ACTIVE registration and admin status-change flow:

```bash
cd backend
./scripts/smoke-signup-and-status.sh
```

Requires Postgres running, app on port 8080, and a seeded tenant (see [docs/IF2AF0203-Bruno-API-Testing.md](docs/IF2AF0203-Bruno-API-Testing.md)).

---

## Database migrations (Flyway)

Schema changes are managed with **Flyway**. Migration SQL lives in one canonical folder:

```text
backend/src/main/resources/db/migration/
├── V1__core_tenant_org_user_rbac.sql
├── …
├── V9__seed_mvp_roles_permissions.sql    # NGO schema (do not edit)
├── V10__auth_tokens_and_soft_delete_audit.sql
├── …
└── V16__drop_stale_users_user_type_check.sql   # backend-owned deltas
```

### Version ownership

| Versions | Owner | Notes |
|----------|-------|-------|
| **V1–V9** | NGO / shared schema | Already applied on AWS. **Do not edit** file contents. |
| **V10–V16** | Backend team | Auth tokens, IF2AF0203 renames, signup/OTP, JPA alignment, constraint fixes. Apply on AWS after V1–V9. |

Fresh local databases run **V1 through V16** in order (no baseline needed).

### File naming convention

```text
V<Version>__<Description>.sql
```

- **Prefix**: `V`
- **Version**: Sequential integer (e.g. `V1`, `V2`, `V16`)
- **Separator**: `__` (two underscores)
- **Description**: `snake_case`
- **Suffix**: `.sql`

### Credential & security guidelines

**DO NOT hardcode database passwords in `flyway.conf` or commit them to Git.**

Flyway's JDBC driver does **not** read `~/.pgpass`. Export `FLYWAY_PASSWORD` before CLI runs:

```bash
export FLYWAY_PASSWORD="your_db_password"

# Optional overrides (defaults are in flyway.conf):
export FLYWAY_URL="jdbc:postgresql://localhost:5432/antarang_psytest"
export FLYWAY_USER="antarang_psytest_user"
```

### Flyway CLI (AWS / manual)

From **repo root** (uses `flyway.conf` → `backend/src/main/resources/db/migration`):

```bash
export FLYWAY_PASSWORD="your-password"
flyway -configFiles=flyway.conf info
flyway -configFiles=flyway.conf migrate
```

### Baselining existing databases

If an environment already had **V1** and **V2** applied manually via `psql -f` *before* Flyway was introduced, baseline first:

```bash
export FLYWAY_PASSWORD="your_actual_password"
flyway -configFiles=flyway.conf baseline
```

`flyway.conf` sets `baselineVersion=2` for legacy NGO environments. **AWS staging/prod** already has **V1–V9** applied — apply **V10+** only; do not re-run V1–V9.

### Day-to-day migration workflow

1. **Status:** `flyway -configFiles=flyway.conf info`
2. **Add migration:** new file under `backend/src/main/resources/db/migration/` (next version number).
3. **Apply:** `flyway -configFiles=flyway.conf migrate`
4. **Repair (dev only):** if a migration fails mid-dev, fix SQL then `flyway -configFiles=flyway.conf repair`

Local dev alternative: start Spring Boot — Flyway runs automatically on startup using the same migration folder.

### AWS SSM deployment runbook

After new migrations are merged to `develop`:

**Step 1 — Connect to EC2**

```bash
aws ssm start-session --target i-02c3516ae6a8ee858 --region ap-south-1
```

**Step 2 — Pull latest**

```bash
cd /home/ssm-user/psychometric-assessment
git checkout develop
git pull origin develop
```

**Step 3 — Export password & migrate**

```bash
export FLYWAY_PASSWORD="your-actual-db-password"
flyway -configFiles=flyway.conf info
flyway -configFiles=flyway.conf migrate
flyway -configFiles=flyway.conf info
```

**One-liner:**

```bash
cd /home/ssm-user/psychometric-assessment && git checkout develop && git pull origin develop && export FLYWAY_PASSWORD="your-actual-db-password" && flyway -configFiles=flyway.conf info && flyway -configFiles=flyway.conf migrate && flyway -configFiles=flyway.conf info
```

### Safety & safeguards

- **`flyway.cleanDisabled=true`** in `flyway.conf` — blocks accidental `flyway clean`.
- PostgreSQL runs DDL in transactions; failed scripts roll back and Flyway records the failure.

---

## Branching strategy

We follow a **trunk-based flow** with a `develop` integration branch. This keeps `main` always stable and deployable, while allowing multiple people to work on features in parallel.

```text
main        → always stable, production-ready
develop     → integration branch; features merge here first
feature/*   → new features or enhancements
fix/*       → non-urgent bug fixes
hotfix/*    → urgent fixes branched directly off main
```

### Branch naming convention

Use lowercase, hyphen-separated names, prefixed by type:

```text
feature/candidate-scoring-module
feature/ocean-model-integration
fix/login-token-expiry
hotfix/report-generation-crash
```

### Branch purposes

| Branch | Branched from | Merges into | Purpose |
|---|---|---|---|
| `main` | — | — | Stable, deployable code only |
| `develop` | `main` | `main` | Integration point for all features before release |
| `feature/*` | `develop` | `develop` | New features, enhancements |
| `fix/*` | `develop` | `develop` | Non-urgent bug fixes |
| `hotfix/*` | `main` | `main` + `develop` | Urgent fixes needed in production immediately |

---

## Day-to-day workflow

### 1. Start a new feature or fix

Always branch off the latest `develop`:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

### 2. Work and commit

```bash
git add .
git commit -m "feat: add candidate scoring logic"
```

### 3. Push your branch

```bash
git push -u origin feature/your-feature-name
```

### 4. Open a Pull Request

Open a PR on GitHub targeting **`develop`** (not `main`). Add a clear description of what changed and why. Request a review from at least one teammate.

### 5. After approval, merge

Once approved and checks pass, merge into `develop`. Delete the feature branch afterward:

```bash
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

### 6. Releasing to `main`

When `develop` is stable and tested, open a PR from `develop → main`. Once merged, optionally tag the release:

```bash
git checkout main
git pull origin main
git tag -a v1.0.0 -m "Initial release"
git push origin v1.0.0
```

### 7. Hotfixes (urgent production issues)

```bash
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-name
# fix, commit, push
```

Open PRs from the hotfix branch into **both** `main` and `develop` so the fix isn't lost on the next release.

---

## Git hooks (path names)

Trailing spaces in folder names (e.g. `Facilitators `) break `git pull` on Windows. A shared **pre-commit** hook rejects bad paths before they enter the repo.

### One-time setup

```bash
./scripts/setup-git-hooks.sh
```

This sets `core.hooksPath` to `.githooks/` for your local clone.

### What gets blocked

- Leading or trailing whitespace in any file or folder name component
- Trailing dots in name components (e.g. `file.`)

Middle spaces are fine (`Photo Library/` is OK). `Facilitators /` is not.

### Bypass (avoid unless intentional)

```bash
git commit --no-verify
```

Use only when you know what you are doing — bad paths will still break Windows checkouts.

---

## Commit message convention

Use short, present-tense, prefixed commit messages:

```text
feat: add OCEAN model scoring endpoint
fix: correct token expiry calculation
docs: update README with branching strategy
refactor: simplify assessment result parser
test: add unit tests for scoring module
chore: update dependencies
```

---

## Pull request guidelines

- Keep PRs focused — one feature/fix per PR
- Write a clear title and description (what changed, why, how to test)
- Link related issues if applicable
- Request at least one review before merging
- Resolve all review comments before merge
- Do not merge your own PR without review, except for trivial doc fixes

---

## Branch protection rules

The following protections are applied on `main` and `develop` (configured under **Settings → Branches**):

- Pull request required before merging (no direct pushes)
- At least 1 approval required
- Status checks must pass before merging (once CI is added)
- Force-pushes and branch deletion disabled

---

## Quick command reference

| Action | Command |
|---|---|
| Clone repo | `git clone <url>` |
| Create + switch to new branch | `git checkout -b <branch-name>` |
| Switch to existing branch | `git checkout <branch-name>` |
| Push new branch first time | `git push -u origin <branch-name>` |
| Pull latest changes | `git pull origin <branch-name>` |
| List local branches | `git branch` |
| List remote branches | `git branch -r` |
| Delete local branch | `git branch -d <branch-name>` |
| Delete remote branch | `git push origin --delete <branch-name>` |
| Check current status | `git status` |
| View commit history | `git log --oneline --graph --all` |
| Run backend (local) | `cd backend && ./mvnw spring-boot:run` |
| Backend tests | `cd backend && ./mvnw clean test` |
| Flyway status (repo root) | `flyway -configFiles=flyway.conf info` |
| Flyway migrate (repo root) | `flyway -configFiles=flyway.conf migrate` |
| Enable git hooks (once) | `./scripts/setup-git-hooks.sh` |

---

## Documentation & change tracking

| Location | Contents |
|----------|----------|
| [docs/](docs/) | IF2AF0203 PDFs, [Bruno API testing guide](docs/IF2AF0203-Bruno-API-Testing.md) |
| [.opsx/changes/](.opsx/changes/) | Sprint proposals (`sprint-1` … `sprint-4`), IF2AF0203 auth alignment (`if2af0203-account-auth`) |

---

## Questions?

Reach out to the repo maintainer or open a discussion/issue on GitHub.
