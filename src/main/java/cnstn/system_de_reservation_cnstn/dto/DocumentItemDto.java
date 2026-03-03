package cnstn.system_de_reservation_cnstn.dto;

public record DocumentItemDto(
        Long id,
        String titre,
        String type,
        String chemin,
        String niveauAcces
) {}