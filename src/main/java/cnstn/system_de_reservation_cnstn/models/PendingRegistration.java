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
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer matricule;
    private Integer telephone;

    @Column(length = 6)
    private String verificationCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Date verificationCodeExpiresAt;

    private Boolean emailVerified;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
}
