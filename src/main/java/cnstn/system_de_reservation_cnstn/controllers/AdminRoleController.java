package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.CreateRoleRequest;
import cnstn.system_de_reservation_cnstn.services.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public List<String> allRoles() {
        return adminRoleService.allRoles();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createRole(@RequestBody CreateRoleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRoleService.createRole(req));
    }
}
