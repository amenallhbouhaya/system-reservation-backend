package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.NotificationDto;
import cnstn.system_de_reservation_cnstn.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public List<NotificationDto> my(Authentication auth) {
        return notificationService.myNotifications(auth.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {
        long count = notificationService.unreadCount(auth.getName());
        return Map.of("count", count);
    }

    @PutMapping("/{id}/read")
    public Map<String, String> markRead(Authentication auth, @PathVariable Long id) {
        notificationService.markRead(auth.getName(), id);
        return Map.of("status", "ok");
    }

    @PutMapping("/read-all")
    public Map<String, String> markAllRead(Authentication auth) {
        notificationService.markAllRead(auth.getName());
        return Map.of("status", "ok");
    }
}
