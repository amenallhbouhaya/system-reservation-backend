package cnstn.system_de_reservation_cnstn.dto.auth;

import cnstn.system_de_reservation_cnstn.models.Role;

public record AuthResponse(String token, Role role) {}