package com.gestorelearning.auth.dto;

import com.gestorelearning.auth.domain.UserRole;

public record LoginResponse(
        String accessToken,
        UserRole role
) {
}
