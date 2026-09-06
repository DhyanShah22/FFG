package com.antarang.cap.dto.request.signup;

import com.antarang.cap.domain.enums.StaffAffiliationType;

import java.util.UUID;

/**
 * IF2AF0203 §II.2.D — Data Analyst sign-up fields (same structure as Administrator).
 */
public record DataAnalystSignupDetails(
        StaffAffiliationType dataAnalystAffiliationType,
        String stateGovernment,
        String department,
        String designation,
        UUID ngoInstitutionOrgUnitId,
        String ngoInstitutionNameManual
) {
}
