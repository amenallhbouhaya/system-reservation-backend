package cnstn.system_de_reservation_cnstn.dto.auth;

import java.util.Date;

public record PendingRegistrationDto(
        Long id,
        String nom,
        String prenom,
        String email,
        Integer matricule,
        Integer telephone,
        Date dateCreation
) {}
