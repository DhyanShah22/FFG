# IF2AF0203 Account Auth — Bruno API Testing Guide

Complete reference for IF2AF0203 **multi-step signup**, login, OTP, and password reset. Use with [Bruno](https://www.usebruno.com/).

**Related:** [IF2AF0203 Account Creation PDF](IF2AF0203-Account%20Creation%20(Sign%20Up)%20and%20Log%20In%20Logic-280726-154118.pdf)

---

## PDF vs backend — implementation status

**Overall IF2AF0203 signup/login: not complete for full PDF workflow testing.** Several flows exist in code but require Bruno end-to-end validation before sign-off.

| PDF requirement | Code status | Ready to test full PDF flow? |
|-----------------|-------------|------------------------------|
| Four role-specific signup forms | Implemented (`signupDetails` per `profileType`) | Partial — use session `PUT` + `complete` |
| Consent before account creation | Implemented — `consent` required in session data; validated at `complete`; `consent_records` row created | Yes — include `consent` in session `sessionData` before complete |
| Email/mobile OTP (4-digit, 150s, 30s resend) | Implemented — `otp/send`, `otp/verify`, `otp/resend`, `otp/expire` on signup sessions | Partial — OTP simulated in server logs |
| Password 13 characters | Implemented — register session, reset-password, admin `POST /users`, profile password change | Yes |
| Google SSO | Implemented — `POST /api/v1/auth/google` | No — requires `GOOGLE_CLIENT_ID` + real ID token |
| 1-hour session / abandonment / back-button | Implemented — TTL on activity; `DELETE /sessions/{id}` abandon (window close); PUT clears verification if email/mobile changed | Partial — client must call abandon on close |
| Password reset 4-digit OTP | Implemented — `forgot-password`, `forgot-password/resend`, `reset-password` with `challengeId` + `otp` | Partial — OTP in logs |
| Explorer mobile password reset | Implemented — `mobileNumber` + `profileType: CAREER_EXPLORER` on forgot-password | Partial |
| Explorer login by username | Implemented — `loginId` matches `email` or `username` (`UserRepository.findByLoginIdWithRoles`) | Needs smoke test |
| Public configuration lookup | Implemented | Yes |
| `educationStageConfigId` | Implemented (V6 seed) | Yes |

This guide documents the **multi-step signup session flow** and related auth APIs. Do not treat the requirement as closed until Bruno E2E passes for each profile type.

---

## Setup

| Setting | Value |
|---------|--------|
| Base URL | `http://localhost:8080` |
| Auth header (protected routes) | `Authorization: Bearer {{accessToken}}` |

**Bruno environment variables:** `baseUrl`, `accessToken`, `refreshToken`, `tenantCode` (`demo`), `genderConfigId`, `gradeConfigId`, `educationStageConfigId`, `signupSessionId`, `otpChallengeId`

**Start app:**

```bash
./mvnw spring-boot:run
```

**Bootstrap tenant (if empty):**

```sql
INSERT INTO tenants (id, code, name, is_active, is_deleted, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'demo', 'Demo Tenant', true, false, NOW(), NOW());
```

**Fetch configuration UUIDs (no auth required):**

```http
GET {{baseUrl}}/api/v1/configurations?groupCode=GENDER
GET {{baseUrl}}/api/v1/configurations?groupCode=GRADE
GET {{baseUrl}}/api/v1/configurations?groupCode=EDUCATION_STAGE
```

Copy `id` from each response into Bruno env vars. Seeds: `GENDER` (MALE/FEMALE/OTHER), `GRADE` (9–12), `EDUCATION_STAGE` (IN_SCHOOL, DROPPED_OUT, etc.).

---

## Multi-step signup flow (IF2AF0203)

Sign-up is **not** a single `POST /register` with the full form. Use a server-side session (1-hour TTL, extended on each OTP or update):

| Step | Method | Path | Purpose |
|------|--------|------|---------|
| 1 | POST | `/api/v1/auth/signup/sessions` | Create session `{ "tenantCode": "demo", "profileType": "CAREER_EXPLORER" }` → save `data.id` as `signupSessionId` |
| 2 | PUT | `/api/v1/auth/signup/sessions/{{signupSessionId}}` | Store form draft: `{ "sessionData": { ...full register fields... } }` |
| 3 | POST | `/api/v1/auth/signup/sessions/{{signupSessionId}}/otp/send` | `{ "channel": "EMAIL" }` or `{ "channel": "MOBILE" }` — OTP logged as `[SIMULATED OTP]` |
| 4 | POST | `/api/v1/auth/signup/sessions/{{signupSessionId}}/otp/verify` | `{ "channel": "EMAIL", "otp": "1234" }` — repeat for mobile if `EMAIL_AND_MOBILE` |
| 5 | POST | `/api/v1/auth/signup/sessions/{{signupSessionId}}/otp/resend` | `{ "channel": "EMAIL" }` — only after 30s |
| 6 | POST | `/api/v1/auth/register` | `{ "signupSessionId": "{{signupSessionId}}" }` **or** POST `/signup/sessions/{id}/complete` |
| — | POST | `/signup/sessions/{id}/otp/expire` | `{ "channel": "EMAIL" }` — supersede active OTP without sending new |
| — | DELETE | `/signup/sessions/{id}` | Abandon (window close) — clears data and expires OTPs |

**Back navigation:** if `PUT` changes `email` or `mobileNumber` after OTP verification, that channel’s verification is cleared and must be repeated.

OTP rules: **4 digits**, valid **150 seconds**, resend after **30 seconds**.

Explorer: verify email and/or mobile per `preferredCommunicationChannels`. Other profiles: email OTP required.

**Google SSO login** (existing users only):

```http
POST {{baseUrl}}/api/v1/auth/google
{ "idToken": "<google-id-token>", "profileType": "CAREER_EXPLORER" }
```

Set `GOOGLE_CLIENT_ID` in env / `application.yaml` for token verification.

---

## Register payload structure

All profile types share **common fields** on the root object. Role-specific fields go under `signupDetails` (send only the block matching `profileType`).

### Common fields (all profile types — IF2AF0203 §II.2)

| JSON field | Doc field | Required | Notes |
|------------|-----------|----------|--------|
| `tenantCode` or `tenantId` | — | Yes | Tenant scope |
| `profileType` | Profile type | Yes | See enum below |
| `email` | Email ID | Conditional | Required for Counsellor, Administrator, Data Analyst; required for Explorer when email channel selected |
| `username` | Username | Optional | Explorer username sign-up path (§II.5.B); use with or without `email` |
| `password` | Password | Yes | **Min 13 characters** (IF2AF0203 §II.5) |
| `firstName` | First Name | Yes | |
| `middleName` | Middle Name | No | Optional all profiles |
| `lastName` | Last Name | No | Optional all profiles |
| `dateOfBirth` | Date of Birth | Yes | Drives guardian vs self consent |
| `genderConfigId` | Gender | Yes | UUID from `GENDER` configuration group |
| `country` | Country | Yes | |
| `state` | State | Yes | State/region name |
| `howDidYouFindOut` | How did you find out about this platform? | Yes | Free text or coded value |
| `mobileNumber` | Mobile | Conditional | Required when Explorer selects `MOBILE` or `EMAIL_AND_MOBILE` channel |
| `consent` | Data consent (§II.3) | Yes | Must include `consentGiven: true`; type must match age |
| `signupDetails` | Role-specific form | Yes | Nested object — see per profile below |
| `primaryOrgUnitId` | — | No | Optional org link |
| `preferredPlatformLanguageId` | — | No | Optional |
| `preferredAssessmentLanguageId` | — | No | Optional |

### `profileType` enum

| Value | IF2AF0203 label |
|-------|-----------------|
| `CAREER_EXPLORER` | Career Explorer |
| `CAREER_COUNSELLOR` | Career Counsellor |
| `DATA_ANALYST` | Data Analyst |
| `ADMINISTRATOR` | Administrator |

### Response wrapper

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "profileType": "CAREER_EXPLORER",
    "status": "ACTIVE",
    "consentRequired": true,
    "consentType": "GUARDIAN"
  },
  "timestamp": "..."
}
```

- `consentRequired: true` + `consentType: "GUARDIAN"` when age &lt; 18.
- `consentType: "SELF"` when age ≥ 18.
- New accounts are created with **`status: ACTIVE`** — login works immediately after signup.

### `consent` object (required on every register)

```json
"consent": {
  "consentGiven": true,
  "consentType": "SELF",
  "consentTextVersion": "2026-27-v1",
  "guardianName": null,
  "guardianContact": null
}
```

- Age &lt; 18 → `consentType` must be `GUARDIAN` and `guardianName` + `guardianContact` required.
- Age ≥ 18 → `consentType` must be `SELF`.
- Registration **fails** if `consentGiven` is not `true` or consent type mismatches DOB.

---

## A. Career Explorer — full payload

**IF2AF0203 §II.2.A**

### Explorer `signupDetails.careerExplorer` fields

| JSON field | Doc field | Required | Notes |
|------------|-----------|----------|--------|
| `preferredCommunicationChannels` | Preferred Communication Channel | Yes | `EMAIL`, `MOBILE`, `EMAIL_AND_MOBILE` |
| `educationStageConfigId` | Stage in Education | Conditional | Required if not drop-out / link-profile path |
| `reasonForDropOut` | Reason for Drop Out | Conditional | Required on drop-out path |
| `linkProfile` | Link Profile | Conditional | `true` → grade + NGO path |
| `gradeConfigId` | Grade | Conditional | UUID from `GRADE` group when `linkProfile: true` |
| `ngoInstitutionName` | NGO/Institution Name | Conditional | When `linkProfile: true` |
| `careerCounsellorId` | Career Counsellor ID | No | Optional UUID |
| `studentUid` | Student UID | No | Hidden; auto-populated when linked |
| `schoolName` | School Name | No | Hidden; auto-filled from UID mapping |

### A1 — In-school / link profile path

**Step 2 — PUT** `{{baseUrl}}/api/v1/auth/signup/sessions/{{signupSessionId}}` with `sessionData` containing:
```json
{
  "sessionData": {
  "email": "explorer.inschool@test.com",
  "password": "SecurePass123!",
  "firstName": "Alex",
  "middleName": "M",
  "lastName": "Explorer",
  "dateOfBirth": "2010-05-15",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Maharashtra",
  "howDidYouFindOut": "School counsellor",
  "mobileNumber": "9876543210",
  "consent": {
    "consentGiven": true,
    "consentType": "GUARDIAN",
    "consentTextVersion": "2026-27-v1",
    "guardianName": "Parent Explorer",
    "guardianContact": "9876543299"
  },
  "signupDetails": {
    "careerExplorer": {
      "preferredCommunicationChannels": ["EMAIL_AND_MOBILE"],
      "linkProfile": true,
      "gradeConfigId": "{{gradeConfigId}}",
      "ngoInstitutionName": "Demo NGO School Network",
      "careerCounsellorId": null,
      "studentUid": null,
      "schoolName": null
    }
  }
  }
}
```

### A2 — Drop-out path

```json
{
  "tenantCode": "demo",
  "profileType": "CAREER_EXPLORER",
  "email": "explorer.dropout@test.com",
  "password": "SecurePass123!",
  "firstName": "Riya",
  "dateOfBirth": "2008-03-20",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Karnataka",
  "howDidYouFindOut": "Social media",
  "mobileNumber": "9876543211",
  "consent": {
    "consentGiven": true,
    "consentType": "GUARDIAN",
    "consentTextVersion": "2026-27-v1",
    "guardianName": "Parent Riya",
    "guardianContact": "9876543298"
  },
  "signupDetails": {
    "careerExplorer": {
      "preferredCommunicationChannels": ["MOBILE"],
      "reasonForDropOut": "Family circumstances",
      "educationStageConfigId": "{{educationStageConfigId}}"
    }
  }
}
```

### A3 — Username-only sign-up (no email on form)

Uses internal placeholder email `username@demo.local` for DB uniqueness. Login with `loginId` = username.

```json
{
  "tenantCode": "demo",
  "profileType": "CAREER_EXPLORER",
  "username": "explorer_user_001",
  "password": "SecurePass123!",
  "firstName": "Username",
  "dateOfBirth": "2009-07-01",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Delhi",
  "howDidYouFindOut": "Friend",
  "consent": {
    "consentGiven": true,
    "consentType": "GUARDIAN",
    "consentTextVersion": "2026-27-v1",
    "guardianName": "Parent Username",
    "guardianContact": "9876543297"
  },
  "signupDetails": {
    "careerExplorer": {
      "preferredCommunicationChannels": ["EMAIL"],
      "linkProfile": true,
      "gradeConfigId": "{{gradeConfigId}}",
      "ngoInstitutionName": "Community Learning Center"
    }
  }
}
```

---

## B. Career Counsellor — full payload

**IF2AF0203 §II.2.B**

### Counsellor `signupDetails.careerCounsellor` fields

| JSON field | Doc field | Required | Notes |
|------------|-----------|----------|--------|
| `counsellorAffiliationType` | Counsellor Type | Yes | `NGO_INSTITUTION` or `INDEPENDENT` |
| `ngoInstitutionOrgUnitId` | NGO/Institution dropdown | Conditional | Org unit UUID when NGO type |
| `ngoInstitutionNameManual` | NGO/Institution manual input | Conditional | When dropdown not used |
| `schoolName` | School Name | No | Hidden; auto from email mapping |

### B1 — NGO / Institution (dropdown)

```json
{
  "tenantCode": "demo",
  "profileType": "CAREER_COUNSELLOR",
  "email": "counsellor.ngo@test.com",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "middleName": "K",
  "lastName": "Counsellor",
  "dateOfBirth": "1990-01-15",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Tamil Nadu",
  "howDidYouFindOut": "NGO referral",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "careerCounsellor": {
      "counsellorAffiliationType": "NGO_INSTITUTION",
      "ngoInstitutionOrgUnitId": "{{ngoOrgUnitId}}",
      "ngoInstitutionNameManual": null,
      "schoolName": null
    }
  }
}
```

### B2 — NGO / Institution (manual name)

```json
{
  "tenantCode": "demo",
  "profileType": "CAREER_COUNSELLOR",
  "email": "counsellor.manual@test.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Counsellor",
  "dateOfBirth": "1988-11-22",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Gujarat",
  "howDidYouFindOut": "Conference",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "careerCounsellor": {
      "counsellorAffiliationType": "NGO_INSTITUTION",
      "ngoInstitutionNameManual": "Rural Education Trust",
      "schoolName": null
    }
  }
}
```

### B3 — Independent

```json
{
  "tenantCode": "demo",
  "profileType": "CAREER_COUNSELLOR",
  "email": "counsellor.independent@test.com",
  "password": "SecurePass123!",
  "firstName": "Priya",
  "dateOfBirth": "1992-06-30",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Rajasthan",
  "howDidYouFindOut": "Professional network",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "careerCounsellor": {
      "counsellorAffiliationType": "INDEPENDENT"
    }
  }
}
```

---

## C. Administrator — full payload

**IF2AF0203 §II.2.C**

### Administrator `signupDetails.administrator` fields

| JSON field | Doc field | Required | Notes |
|------------|-----------|----------|--------|
| `administratorAffiliationType` | Administrator Type | Yes | `STATE_GOVERNMENT` or `NGO_INSTITUTION` |
| `stateGovernment` | State Government | Conditional | When `STATE_GOVERNMENT` |
| `department` | Department | Conditional | When `STATE_GOVERNMENT` |
| `designation` | Designation | Conditional | Both affiliation types |
| `ngoInstitutionOrgUnitId` | NGO/Institution dropdown | Conditional | When `NGO_INSTITUTION` |
| `ngoInstitutionNameManual` | NGO/Institution manual | Conditional | When `NGO_INSTITUTION` |

### C1 — State Government

```json
{
  "tenantCode": "demo",
  "profileType": "ADMINISTRATOR",
  "email": "admin.state@test.com",
  "password": "SecurePass123!",
  "firstName": "Arun",
  "middleName": "S",
  "lastName": "Admin",
  "dateOfBirth": "1985-06-01",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Maharashtra",
  "howDidYouFindOut": "Government program",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "administrator": {
      "administratorAffiliationType": "STATE_GOVERNMENT",
      "stateGovernment": "Maharashtra State Government",
      "department": "Education Department",
      "designation": "Program Officer"
    }
  }
}
```

### C2 — NGO / Institution

```json
{
  "tenantCode": "demo",
  "profileType": "ADMINISTRATOR",
  "email": "admin.ngo@test.com",
  "password": "SecurePass123!",
  "firstName": "Sneha",
  "lastName": "Admin",
  "dateOfBirth": "1987-09-12",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Kerala",
  "howDidYouFindOut": "Partner NGO",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "administrator": {
      "administratorAffiliationType": "NGO_INSTITUTION",
      "ngoInstitutionNameManual": "Youth Skills Foundation",
      "designation": "Centre Administrator"
    }
  }
}
```

---

## D. Data Analyst — full payload

**IF2AF0203 §II.2.D** (same structure as Administrator)

### Data Analyst `signupDetails.dataAnalyst` fields

| JSON field | Doc field | Required |
|------------|-----------|----------|
| `dataAnalystAffiliationType` | Data Analyst Type | Yes |
| `stateGovernment` | State Government | Conditional |
| `department` | Department | Conditional |
| `designation` | Designation | Conditional |
| `ngoInstitutionOrgUnitId` | NGO dropdown | Conditional |
| `ngoInstitutionNameManual` | NGO manual | Conditional |

### D1 — State Government

```json
{
  "tenantCode": "demo",
  "profileType": "DATA_ANALYST",
  "email": "analyst.state@test.com",
  "password": "SecurePass123!",
  "firstName": "Vikram",
  "lastName": "Analyst",
  "dateOfBirth": "1991-04-18",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "West Bengal",
  "howDidYouFindOut": "Job portal",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "dataAnalyst": {
      "dataAnalystAffiliationType": "STATE_GOVERNMENT",
      "stateGovernment": "West Bengal Government",
      "department": "Labour Department",
      "designation": "Data Analyst"
    }
  }
}
```

### D2 — NGO / Institution

```json
{
  "tenantCode": "demo",
  "profileType": "DATA_ANALYST",
  "email": "analyst.ngo@test.com",
  "password": "SecurePass123!",
  "firstName": "Meera",
  "dateOfBirth": "1993-12-05",
  "genderConfigId": "{{genderConfigId}}",
  "country": "India",
  "state": "Punjab",
  "howDidYouFindOut": "Research partner",
  "consent": {
    "consentGiven": true,
    "consentType": "SELF",
    "consentTextVersion": "2026-27-v1"
  },
  "signupDetails": {
    "dataAnalyst": {
      "dataAnalystAffiliationType": "NGO_INSTITUTION",
      "ngoInstitutionOrgUnitId": "{{ngoOrgUnitId}}",
      "designation": "Monitoring & Evaluation Analyst"
    }
  }
}
```

---

## Login (profile-type aware)

**POST** `{{baseUrl}}/api/v1/auth/login`

Use `email` **or** `loginId` (email or Explorer `username`):

```json
{
  "loginId": "explorer_user_001",
  "password": "SecurePass123!",
  "profileType": "CAREER_EXPLORER"
}
```

| Scenario | Expected |
|----------|----------|
| Matching `profileType` + ACTIVE user | `200`, tokens + `user.profileType` |
| Wrong `profileType` | `400`, `errorCode: PROFILE_MISMATCH` |
| Inactive/suspended user | `400`, `errorCode: USER_INACTIVE` |
| Duplicate email on register | `400`, `errorCode: DUPLICATE_RESOURCE` |
| Duplicate mobile on register | `400`, `errorCode: DUPLICATE_RESOURCE` |
| Missing required role fields | `400`, `errorCode: VALIDATION_ERROR` |

---

## Other auth endpoints (unchanged)

| Method | Path | Body |
|--------|------|------|
| POST | `/api/v1/auth/refresh` | `{ "refreshToken": "{{refreshToken}}" }` |
| POST | `/api/v1/auth/logout` | `{ "refreshToken": "{{refreshToken}}" }` + Bearer |
| GET | `/api/v1/auth/me` | Bearer only |
| POST | `/api/v1/auth/google` | `{ "idToken": "...", "profileType": "CAREER_EXPLORER" }` |
| POST | `/api/v1/auth/forgot-password` | `{ "email": "..." }` or `{ "mobileNumber": "...", "profileType": "CAREER_EXPLORER" }` → returns OTP challenge |
| POST | `/api/v1/auth/forgot-password/resend` | `{ "challengeId": "{{otpChallengeId}}" }` |
| POST | `/api/v1/auth/reset-password` | `{ "challengeId": "{{otpChallengeId}}", "otp": "1234", "newPassword": "SecurePass123!" }` |

---

## Enum reference

### `preferredCommunicationChannels` (Explorer)

- `EMAIL`
- `MOBILE`
- `EMAIL_AND_MOBILE`

### `counsellorAffiliationType`

- `NGO_INSTITUTION`
- `INDEPENDENT`

### `administratorAffiliationType` / `dataAnalystAffiliationType`

- `STATE_GOVERNMENT`
- `NGO_INSTITUTION`

---

## Suggested Bruno test order

1. Fetch `genderConfigId` / `gradeConfigId` from configurations API
2. Create signup session → PUT session data → send/verify OTP(s) → complete register
3. Register Counsellor, Administrator, Data Analyst via same session flow
4. Validation failures: missing `signupDetails`, wrong affiliation conditionals, unverified OTP on complete
5. Duplicate email / mobile
6. Login with matching `profileType` (accounts are ACTIVE on signup)
7. Forgot-password OTP → reset-password with `challengeId` + `otp`
8. Explorer mobile password reset via `mobileNumber` + `profileType`
7. `GET /auth/me` — confirm `profileType` in response

---

## Data persistence map

| Data | Stored in |
|------|-----------|
| Names, DOB, gender, email, username, mobile | `users` |
| Country, state (profile) | `user_profiles.country`, `user_profiles.state` |
| Grade | `user_profiles.grade_config_id` |
| Role-specific + `howDidYouFindOut` | `user_profiles.profile_data` (JSONB) |

Example `profile_data` keys: `preferredCommunicationChannels`, `reasonForDropOut`, `counsellorAffiliationType`, `administratorAffiliationType`, `stateGovernment`, `department`, `designation`, `ngoInstitutionName`, `schoolName`, etc.
