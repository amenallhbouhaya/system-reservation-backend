package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.NotificationDto;
import cnstn.system_de_reservation_cnstn.models.Notification;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.NotificationRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public void notifyUser(Utilisateur utilisateur, String message, String type, String targetPath) {
        if (utilisateur == null) return;

        Notification notification = new Notification();
        notification.setUtilisateur(utilisateur);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetPath(targetPath);
        notification.setLu(false);
        notification.setDateCreation(new Date());
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyRole(String role, String message, String type, String targetPath) {
        List<Utilisateur> users = utilisateurRepository.findByRole(role);
        for (Utilisateur user : users) {
            notifyUser(user, message, type, targetPath);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> myNotifications(String email) {
        return notificationRepository.findTop50ByUtilisateurEmailOrderByDateCreationDesc(email)
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getMessage(),
                        n.getType(),
                        n.getTargetPath(),
                        n.isLu(),
                        n.getDateCreation()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return notificationRepository.countByUtilisateurEmailAndLuFalse(email);
    }

    @Transactional
    public void markRead(String email, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        if (notification.getUtilisateur() == null || !email.equals(notification.getUtilisateur().getEmail())) {
            throw new RuntimeException("Forbidden notification access");
        }

        notification.setLu(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(String email) {
        List<Notification> mine = notificationRepository.findTop50ByUtilisateurEmailOrderByDateCreationDesc(email);
        for (Notification notification : mine) {
            if (!notification.isLu()) notification.setLu(true);
        }
        notificationRepository.saveAll(mine);
    }
}
