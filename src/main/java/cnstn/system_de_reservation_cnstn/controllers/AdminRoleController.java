package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.CreateRoleRequest;
import cnstn.system_de_reservation_cnstn.models.AppRole;
import cnstn.system_de_reservation_cnstn.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AppRoleRepository appRoleRepository;

    @GetMapping
    public List<String> allRoles() {
        return appRoleRepository.findAll().stream()
                .map(AppRole::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createRole(@RequestBody CreateRoleRequest req) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", normalized));
    }
}
