package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssignmentType;

import java.util.List;

public record StudentActiveAssessmentConfigurationResponse(
        AssessmentConfigurationGroupResponse group,
        List<AssessmentConfigurationGroupItemResponse> items,
        List<AssessmentConfigurationGroupOutputResponse> outputs,
        AssignmentType matchedAssignmentType,
        AssessmentConfigurationGroupItemResponse matchedItem
) {
}
