package cnstn.system_de_reservation_cnstn.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Direction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;
    private String description;
    @OneToMany(mappedBy = "direction")
    private List<CatDocument>  catDocument;
}

