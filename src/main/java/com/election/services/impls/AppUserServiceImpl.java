package com.election.services.impls;

import com.election.modals.*;
import com.election.modals.requests.AdminRequest;
import com.election.modals.requests.UserRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.responses.UserResponse;
import com.election.modals.responses.VoterResponse;
import com.election.repositories.*;
import com.election.services.AppUserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final VoterRepository voterRepository;
    private final AppAdminServiceImpl appAdminService;
    private final VoterServiceImpl voterService;
    private final AppAdminServiceImpl adminService;
    private final PaginationResponseImpl paginationResponse;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleServiceImpl roleService;
    private final AppAdminRepository adminRepository;

    /**
     * Build Permissions object
     * @param permissionName
     * @param privilege
     * @param role
     * @return
     */
    private Permissions buildPermissions(String permissionName, Privilege privilege, Role role) {
        Permissions permissions = new Permissions();
        permissions.setUserPermission(permissionName);
        permissions.setPrivilege(privilege);
        permissions.setRole(role);
        return permissions;
    }

    @Transactional
    @Override
    public Boolean createUser(UserRequest user) {

        if (userRepository.findByUsername(user.getUsername()).isPresent() ) {
            throw new RuntimeException("user already exists with this username");
        } else if (userRepository.findByContact(user.getContact()).isPresent()) {
            throw new RuntimeException("user already exists with this contact");
        }

        Role savedRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("USER");
                    role.setRoleDescription("This is USER role");
                    return roleRepository.save(role);
                });

        if (permissionRepository.getPermissionsByRole(savedRole).isEmpty()) {

            List<Permissions> permissionsList = new ArrayList<>();

            permissionsList.add(
                    buildPermissions("VOTER_MANAGEMENT",
                            new Privilege(null, "READ", "UPDATE", null),
                            savedRole)
            );

            roleService.createPermissions(permissionsList);
        }

        AppUser appUser = new AppUser();
        appUser.setFullName(user.getFullName());
        appUser.setUsername(user.getUsername());
        appUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        appUser.setContact(user.getContact());
        appUser.setAddress(user.getAddress());
        appUser.setRole(savedRole);

        Long adminId = user.getAdmin().getId();
        appUser.setAdmin(adminRepository.findById(adminId).orElse(null));

        List<Voter> voters = new ArrayList<>();

        if (user.getVoterNumberFrom() != null && user.getVoterNumberTo() != null) {

            List<Voter> rangeVoters = voterRepository.findBySerialRange(
                    user.getVoterNumberFrom(),
                    user.getVoterNumberTo(),
                    adminId
            );

            // 🔥 CHECK IF ANY VOTER IS ALREADY ASSIGNED
            for (Voter voter : rangeVoters) {
                if (Boolean.TRUE.equals(voter.getIsAssigned())) {
                    throw new RuntimeException(
                            "Some voters in the selected serial range are already assigned!"
                    );
                }
            }

            // Assign voters
            for (Voter voter : rangeVoters) {
                voter.setIsAssigned(true);
            }

            voters.addAll(rangeVoters);
        }

        appUser.setVoters(voters);

        voterRepository.saveAll(voters);
        userRepository.save(appUser);

        return true;
    }





    @Override
    public Boolean updateUser(UserRequest user) {

        Optional<AppUser> userOptional = userRepository.findById(user.getId());
        if (userOptional.isEmpty()) {
            return false;
        }

        AppUser appUser = userOptional.get();

        // Update basic details
        appUser.setFullName(user.getFullName());
        appUser.setUsername(user.getUsername());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            appUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }

        appUser.setContact(user.getContact());
        appUser.setAddress(user.getAddress());

        // -----------------------------------------
        // 1️⃣ Unassign old voters from this user
        // -----------------------------------------
        List<Voter> oldVoters = appUser.getVoters();

        for (Voter v : oldVoters) {
            v.setIsAssigned(false);
        }
        voterRepository.saveAll(oldVoters);

        // Clear old mappings
        appUser.getVoters().clear();


        // -----------------------------------------
        // 2️⃣ Assign new voters by ID range
        // -----------------------------------------
        List<Voter> newVoters = new ArrayList<>();

        if (user.getVoterNumberFrom() != null && user.getVoterNumberTo() != null) {

            List<Voter> rangeVoters = voterRepository.findBySerialRange(
                    user.getVoterNumberFrom(),
                    user.getVoterNumberTo(),
                    user.getAdmin().getId()
            );

            // Mark them assigned
            for (Voter voter : rangeVoters) {
                voter.setIsAssigned(true);
            }

            newVoters.addAll(rangeVoters);
        }

        // Set new voters
        appUser.setVoters(newVoters);

        // Save updated voters and user
        voterRepository.saveAll(newVoters);
        userRepository.save(appUser);

        return true;
    }


    @Override
    public UserResponse getUserByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse getById(Long id) {
        return userResponse(userRepository.findById(id).get());
    }

    @Override
    public List<UserResponse> getAllUsers(Long adminId) {
        return userRepository.findByAdmin_Id(adminId).stream().map(this::userResponse).collect(toList());
    }

    @Override
    public List<UserResponse> getUsersByAdmin(Long adminId) {
        List<AppUser> appUserList = userRepository.getAppUsersByAdmin_Id(adminId);
        if (!appUserList.isEmpty()) {
            return appUserList.stream().map(this::userResponse).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public Boolean deleteUser(Long id) {
        return null;
    }

        /**
     * Map AppUser to UserResponse
     * @param user
     * @return UserResponse
     */
    public UserResponse userResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setContact(user.getContact());
        response.setAddress(user.getAddress());
        response.setRole(appAdminService.mapToRoleDto(user.getRole()));
        response.setAdmin(adminService.mapToUserRequest(user.getAdmin()));
        response.setVoters(
                user.getVoters().stream()
                        .map(voterService::voterResponse)
                        .collect(Collectors.toList())
        );
        return response;
    }

    @Override
    public PaginatedResponse<VoterResponse> getVotersByUser(Long userId, int page, int size) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Voter> voters = user.getVoters();

        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), voters.size());

        List<Voter> paginatedList =
                start > voters.size() ? new ArrayList<>() : voters.subList(start, end);

        // Convert Voter → VoterResponse (FIXED)
        List<VoterResponse> voterResponses = paginatedList.stream()
                .map(v -> voterService.voterResponse(v))  // ← No semicolon
                .toList();

        // Create fake Page object
        Page<Voter> fakePage = new PageImpl<>(
                paginatedList,
                pageable,
                voters.size()
        );

        return paginationResponse.buildPaginatedResponse(voterResponses, fakePage);
    }

    @Override
    public Object getVotersByAppUser(Long appUserId) {
        return null;
    }
}
