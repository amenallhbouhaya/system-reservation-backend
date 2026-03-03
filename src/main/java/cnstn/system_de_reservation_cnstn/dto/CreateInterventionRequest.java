package cnstn.system_de_reservation_cnstn.dto;

import java.util.List;

public record CreateInterventionRequest(
        String description,
        List<Long> equipementIds
) {}