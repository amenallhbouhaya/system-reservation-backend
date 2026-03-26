package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.EvenementExternalInvitation;
import cnstn.system_de_reservation_cnstn.models.Salle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String fallbackFrom;

    public boolean sendSimpleEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return false;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to.trim());
            String sender = (fromAddress == null || fromAddress.isBlank()) ? fallbackFrom : fromAddress;
            if (sender != null && !sender.isBlank()) {
                message.setFrom(sender);
            }
            message.setSubject(subject == null ? "" : subject);
            message.setText(body == null ? "" : body);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send email to={}", to, ex);
            return false;
        }
    }

    public boolean sendExternalInvitation(EvenementExternalInvitation inv, Evenement event) {
        if (inv == null || event == null) return false;
        if (inv.getInviteEmail() == null || inv.getInviteEmail().isBlank()) {
            log.warn("External invite missing email. invitationId={}", inv.getId());
            return false;
        }

        String subject = "Invitation a un evenement";
        String body = buildBody(inv, event);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(inv.getInviteEmail());
            String sender = (fromAddress == null || fromAddress.isBlank()) ? fallbackFrom : fromAddress;
            if (sender != null && !sender.isBlank()) {
                message.setFrom(sender);
            } else {
                log.warn("Mail sender is empty. Set app.mail.from or spring.mail.username.");
            }
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("External invite sent. invitationId={} to={}", inv.getId(), inv.getInviteEmail());
            return true;
        } catch (Exception ex) {
            log.error("Failed to send external invite. invitationId={} to={}", inv.getId(), inv.getInviteEmail(), ex);
            return false;
        }
    }

    private String buildBody(EvenementExternalInvitation inv, Evenement event) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStart = event.getDateDebut() != null ? fmt.format(event.getDateDebut()) : "";
        String dateEnd = event.getDateFin() != null ? fmt.format(event.getDateFin()) : "";

        String salleNames = "";
        if (event.getSalle() != null && !event.getSalle().isEmpty()) {
            salleNames = event.getSalle().stream()
                .map(Salle::getNom)
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        String lien = event.getLienEnLigne() == null ? "" : event.getLienEnLigne().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(inv.getInviteNom()).append("\n\n");
        sb.append("Vous etes invite a participer a l'evenement suivant :\n\n");
        sb.append("Titre: ").append(event.getTitre()).append("\n");
        if (!dateStart.isBlank() || !dateEnd.isBlank()) {
            sb.append("Date: ").append(dateStart);
            if (!dateEnd.isBlank()) sb.append(" / ").append(dateEnd);
            sb.append("\n");
        }
        if (!salleNames.isBlank()) sb.append("Salle: ").append(salleNames).append("\n");
        if (!lien.isBlank()) sb.append("Lien: ").append(lien).append("\n");
        if (inv.getReferenceCode() != null && !inv.getReferenceCode().isBlank()) {
            sb.append("Reference: ").append(inv.getReferenceCode()).append("\n");
        }

        sb.append("\nCordialement");
        return sb.toString();
    }
}
