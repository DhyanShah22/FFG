package com.antarang.cap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    /** IF2AF0203: OTP validity in seconds. */
    private int validitySeconds = 150;

    /** IF2AF0203: Resend cooldown in seconds. */
    private int resendDelaySeconds = 30;

    /** IF2AF0203: Signup session inactivity timeout in hours. */
    private int signupSessionHours = 1;

    public int getValiditySeconds() {
        return validitySeconds;
    }

    public void setValiditySeconds(int validitySeconds) {
        this.validitySeconds = validitySeconds;
    }

    public int getResendDelaySeconds() {
        return resendDelaySeconds;
    }

    public void setResendDelaySeconds(int resendDelaySeconds) {
        this.resendDelaySeconds = resendDelaySeconds;
    }

    public int getSignupSessionHours() {
        return signupSessionHours;
    }

    public void setSignupSessionHours(int signupSessionHours) {
        this.signupSessionHours = signupSessionHours;
    }
}
