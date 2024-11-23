package com.bht.ludonova.dto.user;

import com.bht.ludonova.dto.steam.SteamUserDTO;
import com.bht.ludonova.model.enums.Role;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private SteamUserDTO steamUser;
}
