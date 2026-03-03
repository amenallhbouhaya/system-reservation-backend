package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.user.ChangePasswordRequest;
import cnstn.system_de_reservation_cnstn.dto.user.MeResponse;
import cnstn.system_de_reservation_cnstn.dto.user.UpdateMeRequest;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserMeController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public MeResponse me(Authentication auth) {
        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        Long serviceId = (u.getService() != null) ? u.getService().getId() : null;
        String serviceNom = (u.getService() != null) ? u.getService().getNom() : null;

        return new MeResponse(
                u.getId(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getPoste(),
                u.getAdresse(),
                u.getTelephone(),
                u.getMatricule(),
                u.getRole(),
                serviceId,
                serviceNom
        );
    }

    @PutMapping
    public MeResponse updateMe(Authentication auth, @RequestBody UpdateMeRequest req) {
        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setPoste(req.poste());
        u.setAdresse(req.adresse());
        u.setTelephone(req.telephone());

        Utilisateur saved = utilisateurRepository.save(u);

        Long serviceId = (saved.getService() != null) ? saved.getService().getId() : null;
        String serviceNom = (saved.getService() != null) ? saved.getService().getNom() : null;

        return new MeResponse(
                saved.getId(),
                saved.getNom(),
                saved.getPrenom(),
                saved.getEmail(),
                saved.getPoste(),
                saved.getAdresse(),
                saved.getTelephone(),
                saved.getMatricule(),
                saved.getRole(),
                serviceId,
                serviceNom
        );
    }

    @PostMapping("/password")
    public void changePassword(Authentication auth, @RequestBody ChangePasswordRequest req) {
        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if (!passwordEncoder.matches(req.oldPassword(), u.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        u.setPassword(passwordEncoder.encode(req.newPassword()));
        utilisateurRepository.save(u);
    }
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(Authentication auth,
                                            @RequestPart("file") MultipartFile file) throws Exception {

        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        u.setPhoto(file.getBytes());
        u.setPhotoContentType(file.getContentType());
        utilisateurRepository.save(u);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(Authentication auth) {
        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if (u.getPhoto() == null) return ResponseEntity.notFound().build();

        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        if (u.getPhotoContentType() != null) {
            mt = MediaType.parseMediaType(u.getPhotoContentType());
        }

        return ResponseEntity.ok().contentType(mt).body(u.getPhoto());
    }
}