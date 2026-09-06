package com.antarang.cap.domain.entity;

import com.antarang.cap.common.BaseEntity;
import com.antarang.cap.domain.enums.UserStatus;
import com.antarang.cap.domain.enums.ProfileType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"})
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_org_unit_id")
    private OrgUnit primaryOrgUnit;

    @Column(nullable = false)
    private String email;

    @Column(length = 100)
    private String username;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender_config_id")
    private UUID genderConfigId;

    @Column(name = "preferred_platform_language_id")
    private UUID preferredPlatformLanguageId;

    @Column(name = "preferred_assessment_language_id")
    private UUID preferredAssessmentLanguageId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 50)
    private ProfileType profileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public OrgUnit getPrimaryOrgUnit() {
        return primaryOrgUnit;
    }

    public void setPrimaryOrgUnit(OrgUnit primaryOrgUnit) {
        this.primaryOrgUnit = primaryOrgUnit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public UUID getGenderConfigId() {
        return genderConfigId;
    }

    public void setGenderConfigId(UUID genderConfigId) {
        this.genderConfigId = genderConfigId;
    }

    public UUID getPreferredPlatformLanguageId() {
        return preferredPlatformLanguageId;
    }

    public void setPreferredPlatformLanguageId(UUID preferredPlatformLanguageId) {
        this.preferredPlatformLanguageId = preferredPlatformLanguageId;
    }

    public UUID getPreferredAssessmentLanguageId() {
        return preferredAssessmentLanguageId;
    }

    public void setPreferredAssessmentLanguageId(UUID preferredAssessmentLanguageId) {
        this.preferredAssessmentLanguageId = preferredAssessmentLanguageId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
}
