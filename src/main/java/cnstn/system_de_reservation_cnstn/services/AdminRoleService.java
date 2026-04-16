package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.auth.CreateRoleRequest;
import cnstn.system_de_reservation_cnstn.models.AppRole;
import cnstn.system_de_reservation_cnstn.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AppRoleRepository appRoleRepository;

    public List<String> allRoles() {
        return appRoleRepository.findAll().stream()
                .map(AppRole::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public Map<String, String> createRole(CreateRoleRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            throw new RuntimeException("Role name is required");
        }

        String normalized = req.name().trim();
        if (appRoleRepository.existsByName(normalized)) {
            throw new RuntimeException("Role already exists");
        }

        AppRole role = new AppRole();
        role.setName(normalized);
        appRoleRepository.save(role);
        return Map.of("name", normalized);
    }
}
