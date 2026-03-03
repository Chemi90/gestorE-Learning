package com.gestorelearning.auth.dto;

import com.gestorelearning.auth.domain.UserRole;
import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        UserRole role
) {
}
