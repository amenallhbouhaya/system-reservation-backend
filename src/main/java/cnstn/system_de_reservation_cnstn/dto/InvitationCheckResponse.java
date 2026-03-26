package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;

public record InvitationCheckResponse(
        String status,
        String message,
        InvitationViewDto invitation,
        Date usedAt
) {}
