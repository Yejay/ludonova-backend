package com.bht.ludonova.dto.user;

import com.bht.ludonova.model.enums.Role;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private String email;
    private Role role;  // Only used in admin endpoints
}
