package com.antarang.cap.domain.enums;

public final class ProfileRoleMapper {

    private ProfileRoleMapper() {
    }

    public static RoleName toRoleName(ProfileType profileType) {
        return switch (profileType) {
            case CAREER_EXPLORER -> RoleName.CAREER_EXPLORER;
            case CAREER_COUNSELLOR -> RoleName.CAREER_COUNSELLOR;
            case ADMINISTRATOR -> RoleName.ADMINISTRATOR;
            case DATA_ANALYST -> RoleName.DATA_ANALYST;
        };
    }
}
