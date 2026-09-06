package com.antarang.cap.service;

import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserProfile;
import com.antarang.cap.dto.request.RegisterRequest;
import com.antarang.cap.dto.request.signup.AdministratorSignupDetails;
import com.antarang.cap.dto.request.signup.CareerCounsellorSignupDetails;
import com.antarang.cap.dto.request.signup.CareerExplorerSignupDetails;
import com.antarang.cap.dto.request.signup.DataAnalystSignupDetails;
import com.antarang.cap.dto.request.signup.RegisterSignupDetails;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RegisterSignupProfileMapper {

    public UserProfile buildProfile(User user, RegisterRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setCountry(request.country());
        profile.setState(request.state());

        RegisterSignupDetails details = request.signupDetails();
        if (details == null) {
            profile.setProfileData(Map.of("howDidYouFindOut", request.howDidYouFindOut()));
            return profile;
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("howDidYouFindOut", request.howDidYouFindOut());

        if (details.careerExplorer() != null) {
            applyCareerExplorer(profile, profileData, details.careerExplorer());
        }
        if (details.careerCounsellor() != null) {
            applyCareerCounsellor(profileData, details.careerCounsellor());
        }
        if (details.administrator() != null) {
            applyAdministrator(profileData, details.administrator());
        }
        if (details.dataAnalyst() != null) {
            applyDataAnalyst(profileData, details.dataAnalyst());
        }

        profile.setProfileData(profileData);
        return profile;
    }

    private void applyCareerExplorer(
            UserProfile profile,
            Map<String, Object> profileData,
            CareerExplorerSignupDetails ce
    ) {
        if (ce.preferredCommunicationChannels() != null) {
            profileData.put("preferredCommunicationChannels", ce.preferredCommunicationChannels());
        }
        if (ce.educationStageConfigId() != null) {
            profileData.put("educationStageConfigId", ce.educationStageConfigId().toString());
        }
        if (ce.reasonForDropOut() != null) {
            profileData.put("reasonForDropOut", ce.reasonForDropOut());
        }
        if (ce.linkProfile() != null) {
            profileData.put("linkProfile", ce.linkProfile());
        }
        if (ce.gradeConfigId() != null) {
            profile.setGradeConfigId(ce.gradeConfigId());
            profileData.put("gradeConfigId", ce.gradeConfigId().toString());
        }
        if (ce.ngoInstitutionName() != null) {
            profileData.put("ngoInstitutionName", ce.ngoInstitutionName());
        }
        if (ce.careerCounsellorId() != null) {
            profileData.put("careerCounsellorId", ce.careerCounsellorId().toString());
        }
        if (ce.studentUid() != null) {
            profileData.put("studentUid", ce.studentUid());
        }
        if (ce.schoolName() != null) {
            profileData.put("schoolName", ce.schoolName());
        }
    }

    private void applyCareerCounsellor(Map<String, Object> profileData, CareerCounsellorSignupDetails cc) {
        profileData.put("counsellorAffiliationType", cc.counsellorAffiliationType());
        if (cc.ngoInstitutionOrgUnitId() != null) {
            profileData.put("ngoInstitutionOrgUnitId", cc.ngoInstitutionOrgUnitId().toString());
        }
        if (cc.ngoInstitutionNameManual() != null) {
            profileData.put("ngoInstitutionNameManual", cc.ngoInstitutionNameManual());
        }
        if (cc.schoolName() != null) {
            profileData.put("schoolName", cc.schoolName());
        }
    }

    private void applyAdministrator(Map<String, Object> profileData, AdministratorSignupDetails admin) {
        profileData.put("administratorAffiliationType", admin.administratorAffiliationType());
        putStaffAffiliation(profileData, admin.stateGovernment(), admin.department(), admin.designation(),
                admin.ngoInstitutionOrgUnitId(), admin.ngoInstitutionNameManual());
    }

    private void applyDataAnalyst(Map<String, Object> profileData, DataAnalystSignupDetails da) {
        profileData.put("dataAnalystAffiliationType", da.dataAnalystAffiliationType());
        putStaffAffiliation(profileData, da.stateGovernment(), da.department(), da.designation(),
                da.ngoInstitutionOrgUnitId(), da.ngoInstitutionNameManual());
    }

    private void putStaffAffiliation(
            Map<String, Object> profileData,
            String stateGovernment,
            String department,
            String designation,
            java.util.UUID ngoOrgUnitId,
            String ngoManualName
    ) {
        if (stateGovernment != null) {
            profileData.put("stateGovernment", stateGovernment);
        }
        if (department != null) {
            profileData.put("department", department);
        }
        if (designation != null) {
            profileData.put("designation", designation);
        }
        if (ngoOrgUnitId != null) {
            profileData.put("ngoInstitutionOrgUnitId", ngoOrgUnitId.toString());
        }
        if (ngoManualName != null) {
            profileData.put("ngoInstitutionNameManual", ngoManualName);
        }
    }
}
