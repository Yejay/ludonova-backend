package com.bht.ludonova.dto.steam;

import lombok.Data;

import java.util.Map;

@Data
public class SteamAuthRequest {
    private final Map<String, String> openIdParams;
}
