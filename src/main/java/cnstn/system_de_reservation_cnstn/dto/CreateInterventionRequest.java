package cnstn.system_de_reservation_cnstn.dto;

import java.util.List;

public record CreateInterventionRequest(
        String nom,
        String typeAppareil,
        String numeroSerie,
        String descriptionPanne,
        List<Long> equipementIds
) {}