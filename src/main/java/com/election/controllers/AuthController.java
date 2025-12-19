package com.election.controllers;


import com.election.configurations.jwt.JwtUtils;
import com.election.modals.*;
import com.election.modals.requests.JwtRequest;
import com.election.modals.responses.JwtResponse;
import com.election.repositories.*;
import com.election.services.RoleService;
import com.election.services.impls.AppAdminServiceImpl;
import com.election.services.jwt.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final AppAdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final AppAdminServiceImpl userService;
    private final JwtUtils jwtService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostConstruct
    public void createAdmin() {
        Optional<AppAdmin> optionalUser = adminRepository.findByUsername("election@vote.com");

        if (optionalUser.isEmpty()) {

            Role savedRole = roleRepository.findByRoleName("SUPER_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName("SUPER_ADMIN");
                        role.setRoleDescription("This is super admin role");
                        return roleRepository.save(role);
                    });

            if (permissionRepository.getPermissionsByRole(savedRole).isEmpty()) {
                Privilege privilege = new Privilege();
                privilege.setWritePermission("WRITE");
                privilege.setReadPermission("READ");
                privilege.setDeletePermission("DELETE");
                privilege.setUpdatePermission("UPDATE");

                Permissions permissions = new Permissions();
                permissions.setUserPermission("ALL_PERMISSIONS");
                permissions.setRole(savedRole);
                permissions.setPrivilege(privilege);
                roleService.createPermissions(List.of(permissions));
            }

            AppAdmin user = new AppAdmin();
            user.setFullName("Election");
            user.setUsername("election@vote.com");
            user.setPassword(new BCryptPasswordEncoder().encode("123456"));
            user.setRole(savedRole);
            user.setIsActive(true);
            adminRepository.save(user);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest jwtRequest) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            jwtRequest.getUsername(), jwtRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid username or password.");
        }

        // 1️⃣ Try Admin first
        Optional<AppAdmin> admin = adminRepository.findByUsername(jwtRequest.getUsername());
        if (admin.isPresent()) {
            AppAdmin a = admin.get();

            if (!a.getIsActive()) {
                return ResponseEntity.status(403).body("Admin account is disabled.");
            }

            String token = jwtService.generateToken(a.getUsername());

            JwtResponse response = new JwtResponse(
                    a.getId(), a.getFullName(), a.getUsername(),
                    token, roleService.mapToRoleDto(a.getRole())
            );

            return ResponseEntity.ok(response);
        }

        Optional<AppUser> user = appUserRepository.findByUsername(jwtRequest.getUsername());
        if (user.isPresent()) {
            AppUser u = user.get();

            String token = jwtService.generateToken(u.getUsername());

            JwtResponse response = new JwtResponse(
                    u.getId(), u.getFullName(), u.getUsername(),
                    token, roleService.mapToRoleDto(u.getRole())
            );

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body("User not found");
    }


    @PostMapping("/logout")
    public String logout(@RequestParam String username) {
        Optional<AppAdmin> userOptional = adminRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            AppAdmin user = userOptional.get();
            adminRepository.save(user);
            return "User logged out successfully.";
        }
        return null;
    }


    @PostMapping("/translate")
    public Map<String, String> translate(@RequestBody Map<String, String> req) throws Exception {

        String text = req.get("text");
        String projectId = "YOUR_PROJECT_ID"; // your Google Cloud project

        try (TranslationServiceClient client = TranslationServiceClient.create()) {

            LocationName parent = LocationName.of(projectId, "global");

            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setMimeType("text/plain")
                    .setSourceLanguageCode("mr")
                    .setTargetLanguageCode("en")
                    .addContents(text)
                    .build();

            TranslateTextResponse response = client.translateText(request);

            // Get first translation
            String translated = response.getTranslations(0).getTranslatedText();

            Map<String, String> result = new HashMap<>();
            result.put("translatedText", translated);
            return result;
        }
    }

}
