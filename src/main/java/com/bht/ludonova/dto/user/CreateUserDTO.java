//package com.bht.ludonova.dto.user;
//
//import com.bht.ludonova.model.enums.Role;
//import jakarta.validation.constraints.*;
//import lombok.Data;
//
//@Data
//public class CreateUserDTO {
//    @NotBlank(message = "Username is required")
//    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
//    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores and hyphens")
//    private String username;
//
//    @NotBlank(message = "Password is required")
//    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
//    @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
//            message = "Password must contain at least one digit, one lowercase, one uppercase letter and one special character"
//    )
//    private String password;
//
//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
//    private String email;
//
//    @NotNull(message = "Role is required")
//    private Role role = Role.USER;
//}

package com.bht.ludonova.dto.user;

import com.bht.ludonova.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Role is required")
    private Role role = Role.USER;
}