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
public class Equipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date dateAquisation;
    private String etat;
    private Boolean reservable;
    @Enumerated(EnumType.STRING)
    private TypeEquipement typeEquipement;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    @ManyToMany(mappedBy = "equipement")
    @JsonIgnore
    private List<Intervention> intervention = new ArrayList<>();



}
