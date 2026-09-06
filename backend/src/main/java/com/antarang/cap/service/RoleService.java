package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Permission;
import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.dto.response.RoleResponse;
import com.antarang.cap.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private RoleResponse toResponse(Role role) {
        List<String> permissions = role.getPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .toList();
        return new RoleResponse(role.getId(), role.getName().name(), role.getDescription(), permissions);
    }
}
