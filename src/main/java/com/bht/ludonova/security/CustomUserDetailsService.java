package com.bht.ludonova.security;

import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.Role;
import com.bht.ludonova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.debug("Attempting to load user by login: {}", login);

        User user = userRepository.findByUsername(login)
                .orElseGet(() -> userRepository.findByEmail(login)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login)));

        log.debug("Loading user: {} with role: {}", user.getUsername(), user.getRole());

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // Add base USER role
        authorities.add(new SimpleGrantedAuthority(Role.USER.getSpringSecurityRole()));

        // Add ADMIN role if applicable
        if (user.getRole() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getSpringSecurityRole()));
            log.debug("Added ADMIN role for user: {}", user.getUsername());
        }

        log.debug("Final authorities for user {}: {}", user.getUsername(), authorities);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}