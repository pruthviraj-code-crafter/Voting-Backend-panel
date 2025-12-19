package com.election.repositories;

import com.election.modals.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByWardNameAndUserId(String trim, Long adminId);
}
