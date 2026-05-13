package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;

public record AdminCompleteInterventionRequest(
        String observation,
        Date dateReparation
) {}