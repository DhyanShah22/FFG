-- IF2AF0203: multi-step signup sessions (1h) and 4-digit OTP challenges (150s validity, 30s resend).

CREATE TABLE signup_sessions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID         NOT NULL REFERENCES tenants (id),
    profile_type       VARCHAR(50)  NOT NULL,
    session_data       JSONB        NOT NULL DEFAULT '{}',
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    mobile_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at         TIMESTAMPTZ  NOT NULL,
    last_activity_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT signup_sessions_profile_type_check CHECK (
        profile_type IN ('CAREER_EXPLORER', 'CAREER_COUNSELLOR', 'ADMINISTRATOR', 'DATA_ANALYST')
    )
);

CREATE INDEX idx_signup_sessions_expires_at ON signup_sessions (expires_at);

CREATE TABLE otp_challenges (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signup_session_id    UUID REFERENCES signup_sessions (id),
    user_id              UUID REFERENCES users (id),
    purpose              VARCHAR(50)  NOT NULL,
    channel              VARCHAR(20)  NOT NULL,
    destination          VARCHAR(255) NOT NULL,
    otp_hash             VARCHAR(64)  NOT NULL,
    expires_at           TIMESTAMPTZ  NOT NULL,
    resend_available_at  TIMESTAMPTZ  NOT NULL,
    verified_at          TIMESTAMPTZ,
    superseded_at        TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT otp_challenges_purpose_check CHECK (
        purpose IN (
            'SIGNUP_EMAIL',
            'SIGNUP_MOBILE',
            'PASSWORD_RESET_EMAIL',
            'PASSWORD_RESET_MOBILE'
        )
    ),
    CONSTRAINT otp_challenges_channel_check CHECK (
        channel IN ('EMAIL', 'MOBILE')
    )
);

CREATE INDEX idx_otp_challenges_session ON otp_challenges (signup_session_id);
CREATE INDEX idx_otp_challenges_user ON otp_challenges (user_id);
CREATE INDEX idx_otp_challenges_destination ON otp_challenges (destination);
