package cnstn.system_de_reservation_cnstn.dto;

import cnstn.system_de_reservation_cnstn.models.TypeEquipement;

public record EquipementAvailabilityDto(
        Long id,
        String etat,
        Boolean reservable,
        TypeEquipement typeEquipement,
        boolean available
) {}
