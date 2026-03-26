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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public Map<String, String> register(RegisterRequest req) {
        String normalizedEmail = req == null || req.email() == null ? "" : req.email().trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email obligatoire");
        }

        if (utilisateurRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already used");
        }

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(normalizedEmail).orElse(null);
        boolean isUpdate = pending != null;

        if (!isUpdate) {
            pending = new PendingRegistration();
            pending.setDateCreation(new Date());
        }

        pending.setNom(req.nom());
        pending.setPrenom(req.prenom());
        pending.setEmail(normalizedEmail);
        pending.setPassword(passwordEncoder.encode(req.password()));
        pending.setMatricule(req.matricule());
        pending.setTelephone(req.telephone());
        pending.setEmailVerified(false);
        pending.setVerificationCode(generateVerificationCode());
        pending.setVerificationCodeExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000L));
        pendingRegistrationRepository.save(pending);

        boolean sent = emailService.sendSimpleEmail(
            normalizedEmail,
                "Code de verification - CNSTN",
                buildVerificationEmailBody(req.prenom(), pending.getVerificationCode())
        );

        if (!sent) {
            throw new RuntimeException("Impossible d'envoyer le code de verification par email");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", isUpdate
                ? "Un nouveau code de verification a ete envoye."
                : "Code de verification envoye par email. Validez d'abord votre email.");
        response.put("email", normalizedEmail);
        response.put("verificationRequired", "true");
        return response;
    }

    public Map<String, String> verifyRegisterCode(VerifyRegisterCodeRequest req) {
        String email = req == null || req.email() == null ? "" : req.email().trim().toLowerCase();
        String code = req == null || req.code() == null ? "" : req.code().trim();

        if (code.isBlank()) {
            throw new RuntimeException("Code de verification obligatoire");
        }

        PendingRegistration pending;
        if (!email.isBlank()) {
            pending = pendingRegistrationRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Aucune demande d'inscription trouvee pour cet email"));
        } else {
            pending = pendingRegistrationRepository.findByVerificationCode(code)
                    .orElseThrow(() -> new RuntimeException("Code de verification invalide"));
        }

        if (Boolean.TRUE.equals(pending.getEmailVerified())) {
            return Map.of("message", "Email deja verifie. Demande en attente de validation du chef.");
        }

        if (pending.getVerificationCode() == null || !pending.getVerificationCode().equals(code)) {
            throw new RuntimeException("Code de verification invalide");
        }

        Date expiresAt = pending.getVerificationCodeExpiresAt();
        if (expiresAt == null || expiresAt.before(new Date())) {
            throw new RuntimeException("Code de verification expire");
        }

        pending.setEmailVerified(true);
        pending.setVerificationCode(null);
        pending.setVerificationCodeExpiresAt(null);
        pendingRegistrationRepository.save(pending);

        notificationService.notifyRole(
                "chef-hierarchique",
                "Nouvelle demande de creation de compte: " + pending.getEmail(),
                "ACCOUNT_PENDING_APPROVAL",
                "/chef-hierarchique/comptes-en-attente"
        );
        notificationService.notifyRole(
                "ChefHierarchique",
                "Nouvelle demande de creation de compte: " + pending.getEmail(),
                "ACCOUNT_PENDING_APPROVAL",
                "/chef-hierarchique/comptes-en-attente"
        );

        return Map.of("message", "Email verifie. Demande envoyee au chef hierarchique pour validation.");
    }

    public Map<String, String> resendRegisterCode(ResendRegisterCodeRequest req) {
        String email = req == null || req.email() == null ? "" : req.email().trim().toLowerCase();
        if (email.isBlank()) {
            throw new RuntimeException("Email obligatoire");
        }

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucune demande d'inscription trouvee pour cet email"));

        if (Boolean.TRUE.equals(pending.getEmailVerified())) {
            return Map.of("message", "Email deja verifie. Demande en attente de validation du chef.");
        }

        pending.setVerificationCode(generateVerificationCode());
        pending.setVerificationCodeExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000L));
        pendingRegistrationRepository.save(pending);

        boolean sent = emailService.sendSimpleEmail(
                email,
                "Code de verification - CNSTN",
                buildVerificationEmailBody(pending.getPrenom(), pending.getVerificationCode())
        );

        if (!sent) {
            throw new RuntimeException("Impossible d'envoyer le code de verification par email");
        }

        return Map.of("message", "Nouveau code envoye par email.");
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

    private static String generateVerificationCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    private static String buildVerificationEmailBody(String prenom, String code) {
        String displayName = (prenom == null || prenom.isBlank()) ? "" : (" " + prenom.trim());
        return "Bonjour" + displayName + ",\n\n"
                + "Votre code de verification est: " + code + "\n"
                + "Ce code expire dans 10 minutes.\n\n"
                + "Cordialement,\nCNSTN";
    }

}