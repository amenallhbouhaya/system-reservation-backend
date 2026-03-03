package cnstn.system_de_reservation_cnstn.dto.auth;

public record RegisterRequest(
        String nom,
        String prenom,
        String email,
        String password,
        Integer matricule,
        Integer telephone
) {}