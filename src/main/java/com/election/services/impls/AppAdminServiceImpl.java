package com.election.services.impls;

import com.election.modals.*;
import com.election.modals.requests.PermissionDto;
import com.election.modals.requests.RoleDto;
import com.election.modals.requests.AdminRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.responses.VoterResponse;
import com.election.repositories.AppAdminRepository;
import com.election.repositories.PermissionRepository;
import com.election.repositories.RoleRepository;
import com.election.repositories.VoterRepository;
import com.election.services.AppAdminService;
import com.election.services.RoleService;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppAdminServiceImpl implements AppAdminService {

    private final AppAdminRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleService roleService;
    private final VoterRepository voterRepository;
    private final PaginationResponseImpl paginationResponse;
    private final VoterServiceImpl voterService;


    /**
     * Build Permissions Object
     * @param permissionName
     * @param privilege
     * @param role
     * @return Permissions
     */
    private Permissions buildPermissions(String permissionName, Privilege privilege, Role role) {
        Permissions permissions = new Permissions();
        permissions.setUserPermission(permissionName);
        permissions.setPrivilege(privilege);
        permissions.setRole(role);
        return permissions;
    }

    @Override
    public Boolean addUser(AdminRequest user) {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("user already exists with this username");
        } else if (userRepository.findByContact(user.getContact()).isPresent()) {
            throw new RuntimeException("user already exists with this contact");
        }

        // 1. FIND OR CREATE "UMEDVAR" ROLE
        Role savedRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ADMIN");
                    role.setRoleDescription("This is ADMIN role");
                    return roleRepository.save(role);
                });

        // 2. ADD PERMISSIONS ONLY IF ROLE HAS NONE
        if (permissionRepository.getPermissionsByRole(savedRole).isEmpty()) {

            List<Permissions> permissionsList = new ArrayList<>();

            // USER MANAGEMENT
            permissionsList.add(
                    buildPermissions("USER_MANAGEMENT",
                            new Privilege("WRITE", "READ", "UPDATE", "DELETE"),
                            savedRole)
            );

            // VOTER MANAGEMENT
            permissionsList.add(
                    buildPermissions("VOTER_MANAGEMENT",
                            new Privilege(null, "READ", "UPDATE", null),
                            savedRole)
            );

            // REPORT MANAGEMENT
            permissionsList.add(
                    buildPermissions("REPORT_MANAGEMENT",
                            new Privilege(null, "READ", null, null),
                            savedRole)
            );

            roleService.createPermissions(permissionsList);
        }

        // 3. CREATE ADMIN USER AND ASSIGN UMEDVAR ROLE
        AppAdmin userRequest = new AppAdmin();
        userRequest.setFullName(user.getFullName());
        userRequest.setUsername(user.getUsername());
        userRequest.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRequest.setContact(user.getContact());
        userRequest.setAddress(user.getAddress());
        userRequest.setRole(savedRole);   // <-- Assign UMEDVAR

        userRepository.save(userRequest);
        return true;
    }


    @Override
    public Boolean updateUser(AdminRequest user) {
        Optional<AppAdmin> optionalMyUserbyId = userRepository.findById(user.getId());
        if (optionalMyUserbyId.isPresent()) {
            AppAdmin existingUser = optionalMyUserbyId.get();
            existingUser.setFullName(user.getFullName());
            existingUser.setUsername(user.getUsername());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            }
            existingUser.setContact(user.getContact());
            existingUser.setAddress(user.getAddress());
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    @Override
    public User getUserByUsername(String username) {
        return null;
    }

    @Override
    public Boolean deleteUser(Long id) {
        Optional<AppAdmin> optionalMyUser = userRepository.findById(id);
        if (optionalMyUser.isPresent()) {
            userRepository.delete(optionalMyUser.get());
            return true;
        }
        return false;
    }

    @Override
    public Boolean enableUser(Long id) {
        Optional<AppAdmin> optionalMyUser = userRepository.findById(id);
        if (optionalMyUser.isPresent()) {
            AppAdmin user = optionalMyUser.get();
            user.setIsActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public Boolean disableUser(Long id) {
        Optional<AppAdmin> optionalMyUser = userRepository.findById(id);
        if (optionalMyUser.isPresent()) {
            AppAdmin user = optionalMyUser.get();
            user.setIsActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public List<AdminRequest> getUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getRole().getRoleName().equals("SUPER_ADMIN"))
                .map(this::mapToUserRequest)
                .toList();
    }

    @Override
    public AdminRequest getUserById(Long id) {
        return mapToUserRequest(userRepository.findById(id).get());
    }

    /**
     * Map MyUser to UserRequest
     * @param user
     * @return
     */
    public AdminRequest mapToUserRequest(AppAdmin user) {
        AdminRequest adminRequest = new AdminRequest();
        adminRequest.setId(user.getId());
        adminRequest.setFullName(user.getFullName());
        adminRequest.setUsername(user.getUsername());
        adminRequest.setPassword(user.getPassword());
        adminRequest.setAddress(user.getAddress());
        adminRequest.setContact(user.getContact());
        adminRequest.setIsActive(user.getIsActive());
        adminRequest.setRole(user.getRole());
        return adminRequest;
    }

    /**
     * Map Role to RoleDto
     * @param role
     * @return RoleDto
     */
    public RoleDto mapToRoleDto(Role role) {
        RoleDto roleDto = new RoleDto();
        roleDto.setRoleId(role.getId());
        roleDto.setRoleName(role.getRoleName());
        roleDto.setRoleDescription(role.getRoleDescription());

        List<PermissionDto> permissionDtos = new ArrayList<>();
        for (Permissions permission : role.getPermissions()) {
            PermissionDto permissionDto = new PermissionDto();
            List<String> privileges = new ArrayList<>();
            permissionDto.setUserPermission(permission.getUserPermission());
            privileges.add(permission.getPrivilege().getReadPermission());
            privileges.add(permission.getPrivilege().getWritePermission());
            privileges.add(permission.getPrivilege().getUpdatePermission());
            privileges.add(permission.getPrivilege().getDeletePermission());

            permissionDto.setPrivileges(privileges);
            permissionDtos.add(permissionDto);
        }

        roleDto.setPermissions(permissionDtos);
        return roleDto;
    }


    @Override
    public PaginatedResponse<VoterResponse> getVoterByAdminAndUser(Long adminId, Long userId,
                                                                   int page, int pageSize) {

        AppAdmin admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Check if the user belongs to this admin
        boolean userExists = admin.getAppUsers().stream()
                .anyMatch(u -> u.getId().equals(userId));

        if (!userExists) {
            throw new RuntimeException("User does not belong to this admin");
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("voterSerialNumber").ascending());

        // IMPORTANT: Filter using JOIN on appUsers
        Page<Voter> voterPage = voterRepository.findVotersByAdminIdAndUserId(adminId, userId, pageable);

        List<VoterResponse> voterResponses = voterPage.getContent()
                .stream()
                .map(voterService::voterResponse)
                .toList();

        return paginationResponse.buildPaginatedResponse(voterResponses, voterPage);
    }


}
