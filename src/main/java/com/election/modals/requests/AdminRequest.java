package com.election.modals.requests;

import com.election.modals.Role;
import lombok.Data;

@Data
public class AdminRequest {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private String address;
    private String contact;
    private Boolean isActive;
    private Role role;
}
