package com.election.repositories;

import com.election.modals.AppUser;
import com.election.modals.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    List<AppUser> getAppUsersByAdmin_Id(Long adminId);

    List<AppUser> findByAdmin_Id(Long adminId);

    Optional<AppUser> findByContact(String contact);

    @Query("SELECT COUNT(u) > 0 FROM AppUser u WHERE u.id = :userId AND u.admin.id = :adminId")
    boolean isUserAssignedToAdmin(@Param("userId") Long userId, @Param("adminId") Long adminId);

}
