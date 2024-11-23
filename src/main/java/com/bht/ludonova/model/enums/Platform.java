package com.bht.ludonova.model.enums;

import lombok.Getter;


@Getter
public enum Platform {
    // Sony PlayStation
    PS1("PlayStation"),
    PS2("PlayStation 2"),
    PS3("PlayStation 3"),
    PS4("PlayStation 4"),
    PS5("PlayStation 5"),

    // Microsoft Xbox
    XBOX("Xbox"),
    XBOX_360("Xbox 360"),
    XBOX_ONE("Xbox One"),
    XBOX_SERIES_X("Xbox Series X"),
    XBOX_SERIES_S("Xbox Series S"),

    // Nintendo
    SWITCH("Nintendo Switch"),
    WII("Nintendo Wii"),
    WII_U("Nintendo Wii U"),
    NINTENDO_3DS("Nintendo 3DS"),
    NINTENDO_DS("Nintendo DS"),

    // PC Platforms
    PC("PC"),
    MAC("Mac"),
    LINUX("Linux");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

}
