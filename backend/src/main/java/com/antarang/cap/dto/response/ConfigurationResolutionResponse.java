package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssignmentType;

public record ConfigurationResolutionResponse(
        AssessmentConfigurationGroupResponse group,
        AssessmentConfigurationGroupItemResponse matchedItem,
        AssignmentType matchedAssignmentType
) {
}
