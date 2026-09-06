package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.UserStatus;
import com.antarang.cap.dto.request.AssignRolesRequest;
import com.antarang.cap.dto.request.CreateUserRequest;
import com.antarang.cap.dto.request.UpdateProfileRequest;
import com.antarang.cap.dto.request.UpdateUserLanguageRequest;
import com.antarang.cap.dto.request.UpdateUserRequest;
import com.antarang.cap.dto.request.UpdateUserStatusRequest;
import com.antarang.cap.dto.response.UserResponse;
import com.antarang.cap.service.UserRoleService;
import com.antarang.cap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;

    public UserController(UserService userService, UserRoleService userRoleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateCurrentUser(request), "Profile updated successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ProfileType profileType,
            @RequestParam(required = false) UUID orgUnitId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(userService.listUsers(page, size, profileType, orgUnitId, status, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN')")
    public ApiResponse<UserResponse> getUser(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request), "User created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.success(userService.updateUser(id, request), "User updated successfully");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(userService.updateStatus(id, request), "User status updated successfully");
    }

    @PutMapping("/{id}/platform-language")
    public ApiResponse<UserResponse> updatePlatformLanguage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserLanguageRequest request
    ) {
        return ApiResponse.success(userService.updatePlatformLanguage(id, request), "Platform language updated successfully");
    }

    @PutMapping("/{id}/assessment-language")
    public ApiResponse<UserResponse> updateAssessmentLanguage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserLanguageRequest request
    ) {
        return ApiResponse.success(userService.updateAssessmentLanguage(id, request), "Assessment language updated successfully");
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserResponse> assignRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ApiResponse.success(userRoleService.assignRoles(userId, request), "Roles assigned successfully");
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserResponse> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        return ApiResponse.success(userRoleService.removeRole(userId, roleId), "Role removed successfully");
    }
}
