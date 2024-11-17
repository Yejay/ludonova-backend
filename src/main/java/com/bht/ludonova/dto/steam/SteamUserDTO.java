package com.bht.ludonova.dto.steam;

import lombok.Data;

@Data
public class SteamUserDTO {
    private String steamId;
    private String personaName;
    private String profileUrl;
    private String avatarUrl;
}