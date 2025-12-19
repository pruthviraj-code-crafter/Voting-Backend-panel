package com.election.modals.requests;

import com.election.modals.AppAdmin;
import com.election.modals.AppUser;
import com.election.modals.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserRequest {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private String address;
    private String contact;
    private Boolean isActive;
    private AdminRequest admin;
    private Integer voterNumberFrom;
    private Integer voterNumberTo;

}