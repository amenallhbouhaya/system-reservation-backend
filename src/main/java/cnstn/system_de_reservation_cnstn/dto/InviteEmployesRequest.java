package cnstn.system_de_reservation_cnstn.dto;

import java.util.List;

public record InviteEmployesRequest(
        boolean inviteAll,
        List<Long> userIds
) {}
