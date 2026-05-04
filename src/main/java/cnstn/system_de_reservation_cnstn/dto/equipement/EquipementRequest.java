package cnstn.system_de_reservation_cnstn.dto.equipement;

import cnstn.system_de_reservation_cnstn.models.TypeEquipement;

import java.util.Date;

public record EquipementRequest(
        Date dateAquisation,
        String nom,
        String etat,
        Boolean reservable,
        String numeroSerie,
        TypeEquipement typeEquipement
) {
}