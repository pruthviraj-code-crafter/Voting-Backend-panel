package com.election.modals.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private Long roleId;
    private String roleName;
    private String roleDescription;
    private List<PermissionDto> permissions;
}
