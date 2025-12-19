package com.election.controllers;

import com.election.modals.AppUser;
import com.election.modals.requests.UserRequest;
import com.election.services.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@CrossOrigin("*")
public class AppUserController {

    private AppUserService appUserService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(appUserService.createUser(userRequest));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(appUserService.updateUser(userRequest));
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getUserById(@RequestParam Long id) {
        return ResponseEntity.ok(appUserService.getById(id));
    }

    @GetMapping("/get-voters")
    public ResponseEntity<?> getVotersByUser(@RequestParam Long userId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appUserService.getVotersByUser(userId, page, size));
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers(@RequestParam Long adminId) {
        return ResponseEntity.ok(appUserService.getAllUsers(adminId));
    }

    @GetMapping("/get-users-by-admin")
    public ResponseEntity<?> getUsersByAdmin(@RequestParam Long adminId) {
        return ResponseEntity.ok(appUserService.getUsersByAdmin(adminId));
    }

}
