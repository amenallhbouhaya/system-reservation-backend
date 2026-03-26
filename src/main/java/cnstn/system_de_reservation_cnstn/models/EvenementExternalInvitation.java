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
public class EvenementExternalInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 40)
    private String referenceCode;

    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @Column(nullable = false)
    private String inviteNom;

    @Column(nullable = false)
    private String inviteEmail;

    @Column(nullable = false)
    private Date createdAt = new Date();

    private Date sentAt;
    private Date usedAt;
}
