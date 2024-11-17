package com.bht.ludonova.dto.auth;

import com.bht.ludonova.dto.user.UserDTO;
import lombok.Data;

@Data
public class AuthenticationResponse {
    private final TokenResponse tokens;
    private final UserDTO user;
}
