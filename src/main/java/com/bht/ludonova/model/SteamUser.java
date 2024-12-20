package com.bht.ludonova.model;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@Entity
@Table(name = "steam_users")
public class SteamUser {
    @Id
    private String steamId;
    
    private String personaName;
    private String profileUrl;
    private String avatarUrl;
    
    @OneToOne(mappedBy = "steamUser")
    @JsonBackReference
    private User user;
} 