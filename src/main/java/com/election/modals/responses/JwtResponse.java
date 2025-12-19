package com.election.modals.responses;

import com.election.modals.requests.RoleDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JwtResponse {
    private Long id;
    private String username;
    private String name;
    private String token;
    private RoleDto role;

    public JwtResponse(Long id, String fullName, String username, String token, RoleDto roleDto) {
        this.id = id;
        this.name = fullName;
        this.username = username;
        this.token = token;
        this.role = roleDto;
    }
}
