package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;

public record DsnCompleteInterventionRequest(
        String observation,
        Date dateReparation
) {}
