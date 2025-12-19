package com.election.repositories;

import com.election.modals.Permissions;
import com.election.modals.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, Long> {
    List<Permissions> getPermissionsByRole(Role role);
}
