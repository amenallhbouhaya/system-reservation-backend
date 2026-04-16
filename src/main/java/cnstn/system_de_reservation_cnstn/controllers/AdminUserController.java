package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.UpdateRoleRequest;
import cnstn.system_de_reservation_cnstn.dto.auth.UtilisateurDto;
import cnstn.system_de_reservation_cnstn.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UtilisateurDto> allUsers() {
        return adminUserService.allUsers();
    }

    @PutMapping("/{id}/role")
    public UtilisateurDto updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest req) {
        return adminUserService.updateRole(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id, Authentication auth) {
        String authenticatedEmail = auth != null ? auth.getName() : null;
        return adminUserService.deleteUser(id, authenticatedEmail);
    }
}
