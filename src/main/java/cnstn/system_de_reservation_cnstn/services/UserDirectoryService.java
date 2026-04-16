package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.auth.UtilisateurDto;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final UtilisateurRepository utilisateurRepository;

    public List<UtilisateurDto> listAll() {
        return utilisateurRepository.findAll().stream()
                .map(u -> new UtilisateurDto(
                        u.getId(),
                        u.getNom(),
                        u.getPrenom(),
                        u.getEmail(),
                        u.getRole(),
                        u.getMatricule(),
                        u.getTelephone()
                ))
                .toList();
    }
}
