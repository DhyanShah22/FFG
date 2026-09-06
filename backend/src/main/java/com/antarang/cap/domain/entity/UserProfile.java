package com.antarang.cap.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "grade_config_id")
    private UUID gradeConfigId;

    @Column(name = "age_group_config_id")
    private UUID ageGroupConfigId;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_mobile", length = 20)
    private String guardianMobile;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "address_line_1", columnDefinition = "TEXT")
    private String addressLine1;

    @Column(name = "address_line_2", columnDefinition = "TEXT")
    private String addressLine2;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String pincode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_data", columnDefinition = "jsonb")
    private Map<String, Object> profileData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getGradeConfigId() {
        return gradeConfigId;
    }

    public void setGradeConfigId(UUID gradeConfigId) {
        this.gradeConfigId = gradeConfigId;
    }

    public UUID getAgeGroupConfigId() {
        return ageGroupConfigId;
    }

    public void setAgeGroupConfigId(UUID ageGroupConfigId) {
        this.ageGroupConfigId = ageGroupConfigId;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianMobile() {
        return guardianMobile;
    }

    public void setGuardianMobile(String guardianMobile) {
        this.guardianMobile = guardianMobile;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Map<String, Object> getProfileData() {
        return profileData;
    }

    public void setProfileData(Map<String, Object> profileData) {
        this.profileData = profileData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
