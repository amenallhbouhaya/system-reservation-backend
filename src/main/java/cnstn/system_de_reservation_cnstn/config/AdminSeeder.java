package cnstn.system_de_reservation_cnstn.config;

import cnstn.system_de_reservation_cnstn.models.AppRole;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.AppRoleRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.matricule}")
    private int matricule;

    @Value("${app.admin.telephone}")
    private int telephone;

    @Override
    public void run(String... args) {
        List.of("Admin", "Employe", "ResponsableSalle", "ResponsableSecurite", "DirecteurDsn", "chef-hierarchique")
                .forEach(roleName -> {
                    if (!appRoleRepository.existsByName(roleName)) {
                        AppRole role = new AppRole();
                        role.setName(roleName);
                        appRoleRepository.save(role);
                    }
                });

        if (utilisateurRepository.existsByEmail(email)) {
            return;
        }

        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin");
        admin.setPrenom("CNSTN");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("Admin");

        // خاطر DB عندك matricule/telephone ما ينجمش يكونو null
        admin.setMatricule(matricule);
        admin.setTelephone(telephone);

        utilisateurRepository.save(admin);

        System.out.println("✅ Admin created: " + email + " / " + password);
    }
}