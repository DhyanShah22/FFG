package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.NotificationLogStatus;
import com.antarang.cap.dto.request.SendNotificationRequest;
import com.antarang.cap.dto.response.NotificationLogResponse;
import com.antarang.cap.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications/send")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<NotificationLogResponse> send(@Valid @RequestBody SendNotificationRequest request) {
        return ApiResponse.success(notificationService.send(request), "Notification sent successfully");
    }

    @GetMapping("/notification-logs")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<PageResponse<NotificationLogResponse>> listLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) NotificationLogStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(notificationService.listLogs(userId, templateCode, status, page, size));
    }
}
