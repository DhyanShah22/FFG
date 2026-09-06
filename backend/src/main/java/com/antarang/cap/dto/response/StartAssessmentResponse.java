package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;
import com.antarang.cap.domain.enums.TimerMode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StartAssessmentResponse(
        UUID attemptId,
        UUID assessmentId,
        UUID configurationGroupId,
        UUID questionnaireVersionId,
        UUID iarLogicVersionId,
        UUID scoringRuleVersionId,
        UUID reportTemplateVersionId,
        UUID assessmentLanguageId,
        AttemptTimerResponse timer,
        List<AttemptQuestionResponse> questions,
        boolean resumed
) {
}
