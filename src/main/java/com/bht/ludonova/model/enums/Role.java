package com.bht.ludonova.model.enums;

import lombok.Getter;

@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String springSecurityRole;

    Role(String springSecurityRole) {
        this.springSecurityRole = springSecurityRole;
    }

}
