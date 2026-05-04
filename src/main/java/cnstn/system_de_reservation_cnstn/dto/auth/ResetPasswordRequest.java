package cnstn.system_de_reservation_cnstn.dto.auth;

public record ResetPasswordRequest(
        String email,
        String code,
        String newPassword
) {
}
