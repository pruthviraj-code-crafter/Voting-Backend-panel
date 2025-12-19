package com.election.modals.responses;

import com.election.modals.AppAdmin;
import com.election.modals.Role;
import com.election.modals.requests.AdminRequest;
import com.election.modals.requests.RoleDto;
import com.election.modals.requests.VoterRequest;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private String address;
    private String contact;
    private Boolean isActive;
    private RoleDto role;
    private AdminRequest admin;
    private List<VoterResponse> voters;
}
