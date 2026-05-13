package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.PendingRegistrationDto;
import cnstn.system_de_reservation_cnstn.services.ChefHierarchiqueUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chef-hierarchique/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChefHierarchiqueUserController {

    private final ChefHierarchiqueUserService chefHierarchiqueUserService;

    @GetMapping("/pending")
    public List<PendingRegistrationDto> pendingUsers(Authentication auth) {
        return chefHierarchiqueUserService.pendingUsers(auth);
    }

    @PostMapping("/{id}/accept")
    public Map<String, String> accept(Authentication auth, @PathVariable Long id) {
        return chefHierarchiqueUserService.accept(auth, id);
    }

    @PostMapping("/{id}/reject")
    public Map<String, String> reject(Authentication auth, @PathVariable Long id) {
        return chefHierarchiqueUserService.reject(auth, id);
    }
}
