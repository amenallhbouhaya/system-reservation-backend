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
public class EvenementInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 32)
    private String referenceCode;

    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private Date createdAt = new Date();

    private Date usedAt;
}
