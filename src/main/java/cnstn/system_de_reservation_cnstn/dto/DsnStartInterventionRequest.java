package cnstn.system_de_reservation_cnstn.dto;

import cnstn.system_de_reservation_cnstn.models.InterventionRepairMode;

public record DsnStartInterventionRequest(
        InterventionRepairMode repairMode
) {}
