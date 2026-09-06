package com.antarang.cap.dto.request.signup;

import com.antarang.cap.domain.enums.CounsellorAffiliationType;

import java.util.UUID;

/**
 * IF2AF0203 §II.2.B — Career Counsellor sign-up fields.
 */
public record CareerCounsellorSignupDetails(
        CounsellorAffiliationType counsellorAffiliationType,
        /** NGO/Institution dropdown — org unit id when affiliation is NGO_INSTITUTION. */
        UUID ngoInstitutionOrgUnitId,
        /** NGO/Institution manual name when dropdown not used. */
        String ngoInstitutionNameManual,
        /** Hidden/optional — auto-filled from email mapping. */
        String schoolName
) {
}
