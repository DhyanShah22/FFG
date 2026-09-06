package com.antarang.cap.dto.request.signup;

import com.antarang.cap.domain.enums.CommunicationChannel;

import java.util.List;
import java.util.UUID;

/**
 * IF2AF0203 §II.2.A — Career Explorer sign-up fields.
 */
public record CareerExplorerSignupDetails(
        /** Required — configuration id from GENDER group (or tenant gender config). */
        List<CommunicationChannel> preferredCommunicationChannels,
        /** Stage in Education — configuration id (education stage master). */
        UUID educationStageConfigId,
        /** Required when education path is drop-out. */
        String reasonForDropOut,
        /** Link Profile path — required with grade when not drop-out. */
        Boolean linkProfile,
        /** Grade — configuration id from GRADE group. */
        UUID gradeConfigId,
        /** NGO/Institution Name — conditional on in-school / link profile path. */
        String ngoInstitutionName,
        /** Optional — linked Career Counsellor user id. */
        UUID careerCounsellorId,
        /** Hidden/optional — auto-populated by counsellor link; may be sent if pre-known. */
        String studentUid,
        /** Hidden/optional — auto-filled from UID mapping; may be sent if pre-known. */
        String schoolName
) {
}
