package cnstn.system_de_reservation_cnstn.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.sql.results.graph.Fetch;



import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    @Column(nullable = false)
    @NotBlank
    private String titre;
    private String description;
    @Column(nullable = false)

    private Date dateDebut;
    @Column(nullable = false)

    private Date dateFin;
    private String lienEnLigne;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeEvenement typeEvenement;
    @JsonIgnore
    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL)
    private List<Partenaire> partenaire = new ArrayList<>();
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @JsonIgnore
    @OneToMany(mappedBy ="evenement")
    private List<Equipement> equipement ;

    @JsonIgnore
    @OneToMany(mappedBy = "evenement")
    private List<Salle> salle;
    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private EvenementStatut statut;

    @Column(nullable = false)
    private Boolean inviteAll = false;

    @Column(nullable = false)
    private Boolean inviteSent = false;

    @ElementCollection
    @CollectionTable(name = "evenement_invites", joinColumns = @JoinColumn(name = "evenement_id"))
    @Column(name = "user_id")
    private List<Long> invitedUserIds = new ArrayList<>();

    private String commentaire;// سبب الرفض/ملاحظة (اختياري)
    @Column(nullable = false, length = 60)
    private String createurRole;
    /*    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="evenement_id")
    private Evenement evenement;
*/






}
