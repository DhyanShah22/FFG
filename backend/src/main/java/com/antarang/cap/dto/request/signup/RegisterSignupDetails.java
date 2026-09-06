package com.antarang.cap.dto.request.signup;

/**
 * Role-specific sign-up payloads. Send only the block matching {@code profileType}.
 */
public record RegisterSignupDetails(
        CareerExplorerSignupDetails careerExplorer,
        CareerCounsellorSignupDetails careerCounsellor,
        AdministratorSignupDetails administrator,
        DataAnalystSignupDetails dataAnalyst
) {
}
