package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Permission;
import com.antarang.cap.dto.response.PermissionResponse;
import com.antarang.cap.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getDescription()
                ))
                .toList();
    }
}
