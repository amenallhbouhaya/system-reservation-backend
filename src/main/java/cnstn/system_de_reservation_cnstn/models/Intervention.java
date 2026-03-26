package cnstn.system_de_reservation_cnstn.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomDemandeur;
    private String descriptionPanne;
    private String typeAppareil;
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    private InterventionStatus statut;

    private Date dateDemande;
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
        @ManyToMany
    @JoinTable(
            name = "intervention_equipement",
            joinColumns = @JoinColumn(name = "intervention_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    private List<Equipement> equipement = new ArrayList<>();

        @ManyToOne
        @JoinColumn(name = "service_id")
        private Services service;

        private String chefCommentaire;
        private Date chefDecisionAt;

        @Enumerated(EnumType.STRING)
        private InterventionRepairMode repairMode;
        private String dsnObservation;
        private Date dsnDecisionAt;
        private Date dateReparation;


}



