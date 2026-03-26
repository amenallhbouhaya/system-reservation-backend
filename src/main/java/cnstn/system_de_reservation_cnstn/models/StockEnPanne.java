package cnstn.system_de_reservation_cnstn.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEnPanne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomPiece;
    private Date dateAjout;
    private String demandeurNom;
    private Integer demandeurTelephone;
    private Long interventionId;
}
