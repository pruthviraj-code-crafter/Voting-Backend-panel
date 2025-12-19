package com.election.services;

import com.election.customExceptions.ResourceNotFoundException;
import com.election.modals.Permissions;
import com.election.modals.Privilege;
import com.election.modals.Role;
import com.election.modals.requests.RoleDto;
import com.election.modals.responses.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role createRole(RoleDto roleDto);

    List<RoleDto> getAllRoles();
    List<Permissions> createPermissions(List<Permissions> permissions);
    Privilege createPrivilege(Privilege privilege);

    RoleDto getRoleByRoleName(String roleName) throws ResourceNotFoundException;

    Role updateRole(RoleDto roleDto) throws ResourceNotFoundException;

    ApiResponse<?> deleteRole(Long roleId);

    Role findById(Long roleId);

    RoleDto mapToRoleDto(Role role);
}
