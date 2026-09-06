package com.antarang.cap.security;

/**
 * IF2AF0203 account-creation and password-reset password length (§II.5, §III forgot-password).
 */
public final class PasswordPolicy {

    public static final int SIGNUP_AND_RESET_MIN_LENGTH = 13;

    private PasswordPolicy() {
    }
}
