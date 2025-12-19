package com.election.repositories;

import com.election.modals.AppAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppAdminRepository extends JpaRepository<AppAdmin, Long> {
    Optional<AppAdmin> findByUsername(String username);

//    boolean findByContact(String contact);

    Optional<AppAdmin> findByContact(String contact);
}
