package cnstn.system_de_reservation_cnstn.dto.user;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}