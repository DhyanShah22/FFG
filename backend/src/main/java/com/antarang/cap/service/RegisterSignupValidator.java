package com.antarang.cap.service;

import com.antarang.cap.domain.enums.CommunicationChannel;
import com.antarang.cap.domain.enums.CounsellorAffiliationType;
import com.antarang.cap.domain.enums.ConsentType;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.StaffAffiliationType;
import com.antarang.cap.dto.request.RegisterRequest;
import com.antarang.cap.dto.request.signup.AdministratorSignupDetails;
import com.antarang.cap.dto.request.signup.CareerCounsellorSignupDetails;
import com.antarang.cap.dto.request.signup.CareerExplorerSignupDetails;
import com.antarang.cap.dto.request.signup.DataAnalystSignupDetails;
import com.antarang.cap.dto.request.signup.RegisterSignupDetails;
import com.antarang.cap.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisterSignupValidator {

    public void validate(RegisterRequest request) {
        ProfileType profileType = request.profileType() != null
                ? request.profileType()
                : ProfileType.CAREER_EXPLORER;

        validateCommon(request, profileType);
        validateConsent(request);

        RegisterSignupDetails details = request.signupDetails();
        switch (profileType) {
            case CAREER_EXPLORER -> validateCareerExplorer(request, details);
            case CAREER_COUNSELLOR -> validateCareerCounsellor(request, details);
            case ADMINISTRATOR -> validateAdministrator(request, details);
            case DATA_ANALYST -> validateDataAnalyst(request, details);
        }
    }

    private void validateConsent(RegisterRequest request) {
        if (request.consent() == null) {
            throw new BusinessException("Consent is required before account creation", "VALIDATION_ERROR");
        }
        if (!Boolean.TRUE.equals(request.consent().consentGiven())) {
            throw new BusinessException("Consent must be given to create an account", "VALIDATION_ERROR");
        }
        boolean guardianRequired = request.dateOfBirth() != null
                && java.time.Period.between(request.dateOfBirth(), java.time.LocalDate.now()).getYears() < 18;
        ConsentType expected = guardianRequired ? ConsentType.GUARDIAN : ConsentType.SELF;
        if (request.consent().consentType() != expected) {
            throw new BusinessException(
                    "Consent type must be " + expected.name() + " based on date of birth",
                    "VALIDATION_ERROR"
            );
        }
        if (expected == ConsentType.GUARDIAN) {
            if (request.consent().guardianName() == null || request.consent().guardianName().isBlank()) {
                throw new BusinessException("Guardian name is required for guardian consent", "VALIDATION_ERROR");
            }
            if (request.consent().guardianContact() == null || request.consent().guardianContact().isBlank()) {
                throw new BusinessException("Guardian contact is required for guardian consent", "VALIDATION_ERROR");
            }
        }
    }

    private void validateCommon(RegisterRequest request, ProfileType profileType) {
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new BusinessException("First name is required", "VALIDATION_ERROR");
        }
        if (request.dateOfBirth() == null) {
            throw new BusinessException("Date of birth is required", "VALIDATION_ERROR");
        }
        if (request.genderConfigId() == null) {
            throw new BusinessException("Gender is required", "VALIDATION_ERROR");
        }
        if (request.country() == null || request.country().isBlank()) {
            throw new BusinessException("Country is required", "VALIDATION_ERROR");
        }
        if (request.state() == null || request.state().isBlank()) {
            throw new BusinessException("State is required", "VALIDATION_ERROR");
        }
        if (request.howDidYouFindOut() == null || request.howDidYouFindOut().isBlank()) {
            throw new BusinessException("How did you find out about this platform is required", "VALIDATION_ERROR");
        }

        boolean emailBlank = request.email() == null || request.email().isBlank();
        boolean usernameBlank = request.username() == null || request.username().isBlank();

        if (profileType == ProfileType.CAREER_EXPLORER && emailBlank && usernameBlank) {
            throw new BusinessException("Email or username is required for Career Explorer sign-up", "VALIDATION_ERROR");
        }
        if (profileType != ProfileType.CAREER_EXPLORER && emailBlank) {
            throw new BusinessException("Email is required", "VALIDATION_ERROR");
        }
    }

    private void validateCareerExplorer(RegisterRequest request, RegisterSignupDetails details) {
        if (details == null || details.careerExplorer() == null) {
            throw new BusinessException("Career Explorer sign-up details are required", "VALIDATION_ERROR");
        }
        CareerExplorerSignupDetails ce = details.careerExplorer();

        List<CommunicationChannel> channels = ce.preferredCommunicationChannels();
        if (channels == null || channels.isEmpty()) {
            throw new BusinessException("Preferred communication channel is required", "VALIDATION_ERROR");
        }
        if (channels.contains(CommunicationChannel.MOBILE) || channels.contains(CommunicationChannel.EMAIL_AND_MOBILE)) {
            if (request.mobileNumber() == null || request.mobileNumber().isBlank()) {
                throw new BusinessException("Mobile number is required for the selected communication channel", "VALIDATION_ERROR");
            }
        }
        if (channels.contains(CommunicationChannel.EMAIL) || channels.contains(CommunicationChannel.EMAIL_AND_MOBILE)) {
            if (request.email() == null || request.email().isBlank()) {
                throw new BusinessException("Email is required for the selected communication channel", "VALIDATION_ERROR");
            }
        }

        boolean dropOutPath = ce.reasonForDropOut() != null && !ce.reasonForDropOut().isBlank();
        boolean linkProfilePath = Boolean.TRUE.equals(ce.linkProfile());

        if (dropOutPath) {
            return;
        }
        if (linkProfilePath) {
            if (ce.gradeConfigId() == null) {
                throw new BusinessException("Grade is required when linking profile", "VALIDATION_ERROR");
            }
            if (ce.ngoInstitutionName() == null || ce.ngoInstitutionName().isBlank()) {
                throw new BusinessException("NGO/Institution name is required when linking profile", "VALIDATION_ERROR");
            }
            return;
        }
        if (ce.educationStageConfigId() == null) {
            throw new BusinessException("Stage in education is required", "VALIDATION_ERROR");
        }
    }

    private void validateCareerCounsellor(RegisterRequest request, RegisterSignupDetails details) {
        if (details == null || details.careerCounsellor() == null) {
            throw new BusinessException("Career Counsellor sign-up details are required", "VALIDATION_ERROR");
        }
        CareerCounsellorSignupDetails cc = details.careerCounsellor();
        if (cc.counsellorAffiliationType() == null) {
            throw new BusinessException("Counsellor type is required", "VALIDATION_ERROR");
        }
        if (cc.counsellorAffiliationType() == CounsellorAffiliationType.NGO_INSTITUTION) {
            boolean hasDropdown = cc.ngoInstitutionOrgUnitId() != null;
            boolean hasManual = cc.ngoInstitutionNameManual() != null && !cc.ngoInstitutionNameManual().isBlank();
            if (!hasDropdown && !hasManual) {
                throw new BusinessException(
                        "NGO/Institution name (dropdown or manual) is required for NGO/Institution counsellor type",
                        "VALIDATION_ERROR"
                );
            }
        }
    }

    private void validateAdministrator(RegisterRequest request, RegisterSignupDetails details) {
        if (details == null || details.administrator() == null) {
            throw new BusinessException("Administrator sign-up details are required", "VALIDATION_ERROR");
        }
        validateStaffAffiliation(details.administrator().administratorAffiliationType(),
                details.administrator().stateGovernment(),
                details.administrator().department(),
                details.administrator().designation(),
                details.administrator().ngoInstitutionOrgUnitId(),
                details.administrator().ngoInstitutionNameManual(),
                "Administrator");
    }

    private void validateDataAnalyst(RegisterRequest request, RegisterSignupDetails details) {
        if (details == null || details.dataAnalyst() == null) {
            throw new BusinessException("Data Analyst sign-up details are required", "VALIDATION_ERROR");
        }
        validateStaffAffiliation(details.dataAnalyst().dataAnalystAffiliationType(),
                details.dataAnalyst().stateGovernment(),
                details.dataAnalyst().department(),
                details.dataAnalyst().designation(),
                details.dataAnalyst().ngoInstitutionOrgUnitId(),
                details.dataAnalyst().ngoInstitutionNameManual(),
                "Data Analyst");
    }

    private void validateStaffAffiliation(
            StaffAffiliationType affiliationType,
            String stateGovernment,
            String department,
            String designation,
            java.util.UUID ngoOrgUnitId,
            String ngoManualName,
            String roleLabel
    ) {
        if (affiliationType == null) {
            throw new BusinessException(roleLabel + " type is required", "VALIDATION_ERROR");
        }
        if (affiliationType == StaffAffiliationType.STATE_GOVERNMENT) {
            if (stateGovernment == null || stateGovernment.isBlank()) {
                throw new BusinessException("State Government is required", "VALIDATION_ERROR");
            }
            if (department == null || department.isBlank()) {
                throw new BusinessException("Department is required", "VALIDATION_ERROR");
            }
            if (designation == null || designation.isBlank()) {
                throw new BusinessException("Designation is required", "VALIDATION_ERROR");
            }
        }
        if (affiliationType == StaffAffiliationType.NGO_INSTITUTION) {
            boolean hasDropdown = ngoOrgUnitId != null;
            boolean hasManual = ngoManualName != null && !ngoManualName.isBlank();
            if (!hasDropdown && !hasManual) {
                throw new BusinessException(
                        "NGO/Institution name (dropdown or manual) is required",
                        "VALIDATION_ERROR"
                );
            }
            if (designation == null || designation.isBlank()) {
                throw new BusinessException("Designation is required", "VALIDATION_ERROR");
            }
        }
    }
}
