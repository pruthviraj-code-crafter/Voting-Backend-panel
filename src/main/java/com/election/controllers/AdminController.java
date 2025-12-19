package com.election.controllers;

import com.election.customExceptions.ResourceNotFoundException;
import com.election.modals.requests.AdminRequest;
import com.election.modals.requests.RoleDto;
import com.election.modals.responses.ApiResponse;
import com.election.services.AppAdminService;
import com.election.services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@CrossOrigin("*")
public class AdminController {

    private final AppAdminService appAdminService;
    private final RoleService roleService;

    @PostMapping("/save")
    public ResponseEntity<?> saveUser(@RequestBody AdminRequest user) {
        try {
            return ResponseEntity.ok(appAdminService.addUser(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody AdminRequest user) {
        try {
            return ResponseEntity.ok(appAdminService.updateUser(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @PutMapping("/enable-user")
    public ResponseEntity<?> enableUser(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(appAdminService.enableUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @PutMapping("/disable-user")
    public ResponseEntity<?> disableUser(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(appAdminService.disableUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(appAdminService.getUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getUserById(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(appAdminService.getUserById(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(appAdminService.deleteUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-all-roles")
    public ResponseEntity<?> getAllRoles(){
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PostMapping("/create-role")
    public ResponseEntity<?> createRole(@RequestBody RoleDto role){
        return ResponseEntity.ok(roleService.createRole(role));
    }

    @GetMapping("/get-role-by-role-name")
    public ResponseEntity<?> getRoleByRoleName(@RequestParam String roleName)  {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleByRoleName(roleName));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    @PutMapping("/update-role")
    public ResponseEntity<?> updateRole(@RequestBody RoleDto roleDto) throws ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.updateRole(roleDto));
    }

    @DeleteMapping("/delete-role")
    public ResponseEntity<?> deleteRole(@RequestParam Long roleId){
        ApiResponse<?> response = roleService.deleteRole(roleId);
        if(response.isSuccess()){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/find-role-by-id")
    public ResponseEntity<?> findRoleById(@RequestParam Long roleId) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.findById(roleId));
    }

    @GetMapping("/get-voter-by-user-and-admin")
    public ResponseEntity<?> getVoterByUserAndAdmin(@RequestParam Long userId,
                                                    @RequestParam Long adminId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(appAdminService.getVoterByAdminAndUser(adminId, userId, page, size));
    }
}
