package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.user.ChangePasswordRequest;
import cnstn.system_de_reservation_cnstn.dto.user.MeResponse;
import cnstn.system_de_reservation_cnstn.dto.user.UpdateMeRequest;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserMeService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public MeResponse me(String email) {
        Utilisateur utilisateur = findByEmail(email);
        return toMeResponse(utilisateur);
    }

    public MeResponse updateMe(String email, UpdateMeRequest req) {
        Utilisateur utilisateur = findByEmail(email);

        utilisateur.setNom(req.nom());
        utilisateur.setPrenom(req.prenom());
        utilisateur.setPoste(req.poste());
        utilisateur.setAdresse(req.adresse());
        utilisateur.setTelephone(req.telephone());

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return toMeResponse(saved);
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        Utilisateur utilisateur = findByEmail(email);

        if (!passwordEncoder.matches(req.oldPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        utilisateur.setPassword(passwordEncoder.encode(req.newPassword()));
        utilisateurRepository.save(utilisateur);
    }

    public void uploadPhoto(String email, MultipartFile file) throws IOException {
        Utilisateur utilisateur = findByEmail(email);
        utilisateur.setPhoto(file.getBytes());
        utilisateur.setPhotoContentType(file.getContentType());
        utilisateurRepository.save(utilisateur);
    }

    public ResponseEntity<byte[]> getPhoto(String email) {
        Utilisateur utilisateur = findByEmail(email);

        if (utilisateur.getPhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (utilisateur.getPhotoContentType() != null) {
            mediaType = MediaType.parseMediaType(utilisateur.getPhotoContentType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(utilisateur.getPhoto());
    }

    private Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email).orElseThrow();
    }

    private static MeResponse toMeResponse(Utilisateur utilisateur) {
        return new MeResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getPoste(),
                utilisateur.getAdresse(),
                utilisateur.getTelephone(),
                utilisateur.getMatricule(),
            utilisateur.getRole()
        );
    }
}