package com.bht.ludonova.model.enums;

import lombok.Getter;

@Getter
public enum Platform {
    STEAM("PC"),
    PLAYSTATION("Console"),
    XBOX("Console"),
    NINTENDO_SWITCH("Console"),
    PC_OTHER("PC");

    private final String type;

    Platform(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
