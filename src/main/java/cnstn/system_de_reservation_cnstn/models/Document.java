package cnstn.system_de_reservation_cnstn.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String type;
    private String chemin;
    private String niveauAcces;

    @ManyToOne
    @JoinColumn(name = "catdocument_id")
    private CatDocument catDocument;

    @ManyToMany(mappedBy = "documents")
    @JsonIgnore
    private List<Utilisateur> utilisateurs = new ArrayList<>();


}

