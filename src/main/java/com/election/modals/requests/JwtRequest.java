package com.election.modals.requests;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
}
