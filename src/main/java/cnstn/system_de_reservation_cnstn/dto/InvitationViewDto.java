package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;

public record InvitationViewDto(
        Long evenementId,
        String titre,
        String description,
        String salle,
        Date dateDebut,
        Date dateFin,
        String organisateur,
        String destinataire,
        String referenceCode,
        Date usedAt,
        String email,
        Integer telephone
) {}
