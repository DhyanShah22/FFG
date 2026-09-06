package com.antarang.cap.dto.response;

import java.util.List;

public record AssessmentConfigurationGroupDetailResponse(
        AssessmentConfigurationGroupResponse group,
        List<AssessmentConfigurationGroupItemResponse> items,
        List<AssessmentConfigurationGroupOutputResponse> outputs,
        List<AssessmentConfigurationGroupAssignmentResponse> assignments
) {
}
