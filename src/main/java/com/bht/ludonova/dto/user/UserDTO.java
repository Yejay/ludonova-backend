package com.bht.ludonova.dto.user;

import com.bht.ludonova.dto.steam.SteamUserDTO;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private SteamUserDTO steamUser;
}
