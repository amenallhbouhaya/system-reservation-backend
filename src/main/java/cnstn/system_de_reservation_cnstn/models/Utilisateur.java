package cnstn.system_de_reservation_cnstn.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String nom;
    private String prenom;
    @Column(unique = true, nullable = false)
    private String email;
    private String poste;
    private String adresse;
    @Lob
    @JsonIgnore
    private byte[] photo;

    private String photoContentType;
    private Integer telephone;
    private Integer matricule;
    @JsonIgnore //bech ma yo5rejch f il json
    private String password;
    @Column(nullable = false, length = 60)
    private String role;
    @Column(name = "approuve", nullable = false)
    private Boolean approuve = true;
    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur")
    private List<Intervention> intervention;


    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Evenement> evenements;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_document",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private List<Document> documents = new ArrayList<>();



}

