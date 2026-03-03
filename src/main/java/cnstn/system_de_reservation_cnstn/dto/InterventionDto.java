package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;
import java.util.List;

public record InterventionDto(
        Long id,
        String description,
        String statut,
        Date dateDemande,
        Long demandeurId,
        String demandeurEmail,
        List<Long> equipementIds
) {}