package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;
import java.util.List;

public record InterventionDto(
        Long id,
        String nomDemandeur,
        String descriptionPanne,
        String typeAppareil,
        String numeroSerie,
        String statut,
        Date dateDemande,
        Long demandeurId,
        String demandeurNom,
        String demandeurPrenom,
        String demandeurEmail,
        String serviceNom,
        List<Long> equipementIds,
        String chefCommentaire,
        String repairMode,
        String dsnObservation,
        Date dateReparation
) {}