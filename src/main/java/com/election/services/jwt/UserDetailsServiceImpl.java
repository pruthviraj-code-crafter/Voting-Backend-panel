package com.election.services.jwt;

import com.election.modals.AppAdmin;
import com.election.modals.AppUser;
import com.election.repositories.AppAdminRepository;
import com.election.repositories.AppUserRepository;
import com.election.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppAdminRepository adminRepository;
    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Try Admin first
        Optional<AppAdmin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            AppAdmin a = admin.get();
            GrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + a.getRole().getRoleName());

            return new User(a.getUsername(), a.getPassword(), Collections.singletonList(authority));
        }

        // 2. Try AppUser
        Optional<AppUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            AppUser u = user.get();
            GrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + u.getRole().getRoleName());

            return new User(u.getUsername(), u.getPassword(), Collections.singletonList(authority));
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
