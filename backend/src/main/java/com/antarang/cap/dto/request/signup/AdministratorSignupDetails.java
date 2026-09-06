package com.antarang.cap.dto.request.signup;

import com.antarang.cap.domain.enums.StaffAffiliationType;

import java.util.UUID;

/**
 * IF2AF0203 §II.2.C — Administrator sign-up fields.
 */
public record AdministratorSignupDetails(
        StaffAffiliationType administratorAffiliationType,
        /** Required when affiliation is STATE_GOVERNMENT. */
        String stateGovernment,
        /** Required when affiliation is STATE_GOVERNMENT. */
        String department,
        /** Required when affiliation is STATE_GOVERNMENT or NGO_INSTITUTION. */
        String designation,
        /** NGO/Institution dropdown when affiliation is NGO_INSTITUTION. */
        UUID ngoInstitutionOrgUnitId,
        /** NGO/Institution manual input when affiliation is NGO_INSTITUTION. */
        String ngoInstitutionNameManual
) {
}
