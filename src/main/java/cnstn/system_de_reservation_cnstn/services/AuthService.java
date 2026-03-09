package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.auth.*;
import cnstn.system_de_reservation_cnstn.models.PendingRegistration;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.PendingRegistrationRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import cnstn.system_de_reservation_cnstn.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public String register(RegisterRequest req) {
        if (utilisateurRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already used");
        }

        if (pendingRegistrationRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Demande déjà envoyée, en attente de validation");
        }

        PendingRegistration pending = new PendingRegistration();
        pending.setNom(req.nom());
        pending.setPrenom(req.prenom());
        pending.setEmail(req.email());
        pending.setPassword(passwordEncoder.encode(req.password()));
        pending.setMatricule(req.matricule());
        pending.setTelephone(req.telephone());
        pending.setDateCreation(new Date());
        pendingRegistrationRepository.save(pending);

        notificationService.notifyRole(
                "chef-hierarchique",
                "Nouvelle demande de création de compte: " + req.email(),
                "ACCOUNT_PENDING_APPROVAL",
                "/chef-hierarchique/comptes-en-attente"
        );
        notificationService.notifyRole(
                "ChefHierarchique",
                "Nouvelle demande de création de compte: " + req.email(),
                "ACCOUNT_PENDING_APPROVAL",
                "/chef-hierarchique/comptes-en-attente"
        );

        return "Demande envoyée au chef hiérarchique. Votre compte sera créé après validation.";
    }

    public AuthResponse login(LoginRequest req) {
        Utilisateur u = utilisateurRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));

        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new RuntimeException("Bad credentials");
        }

        String token = jwtService.generateToken(u);
        return new AuthResponse(token, u.getRole());
    }

}