package com.election.repositories;

import com.election.modals.AppAdmin;
import com.election.modals.Voter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoterRepository extends JpaRepository<Voter, Long> {

    List<Voter> findAll(Specification<Voter> spec);

    List<Voter> findByIsDeadFalseOrderByHouseNumberAsc();

    @Query("SELECT v.houseNumber FROM Voter v WHERE v.isDead = false AND v.admin.id = :userId AND v.houseNumber IS NOT NULL AND v.houseNumber <> '' AND v.houseNumber <> '-' GROUP BY v.houseNumber HAVING COUNT(v.id) > 1 ORDER BY v.houseNumber ASC")
    List<String> findHouseNumbersHavingMultipleVoters(Long userId);


    Page<Voter> findAllByAdmin_Id(Long userId, Pageable pageable);

    List<Voter> findByAdmin_Id(Long userId);

    Page<Voter> findByIsVotedTrueAndAdmin_Id(Long userId, Pageable pageable);

    Page<Voter> findByIsVotedFalseAndAdmin_Id(Long userId, Pageable pageable);

    Page<Voter> findAllByAdmin_IdAndNumberIsNotNullAndNumberNot(Long userId, String empty, Pageable pageable);

    // Without mobile number (null or empty)
    @Query("SELECT v FROM Voter v WHERE v.admin.id = :userId AND (v.number IS NULL OR v.number = '')")
    Page<Voter> findAllByUser_IdAndNumberIsNullOrEmpty(@Param("userId") Long userId, Pageable pageable);

    Page<Voter> findByIsDeadTrueAndAdmin_Id(Long userId, Pageable pageable);

    List<Voter> findAllByAdmin_IdAndIsDeadFalse(Long userId);

    List<Voter> findByVoterNumberBetween(Integer start, Integer end);

    List<Voter> findByIdBetween(Long from, Long to);

    @Query("SELECT v.id FROM Voter v WHERE v.isAssigned = false AND v.admin.id = :adminId")
    List<Long> findUnassignedVoterIdsByAdmin(@Param("adminId") Long adminId);

    @Query(value = "SELECT v.voter_serial_number FROM voter v WHERE v.is_assigned = true AND v.admin_id = :adminId ORDER BY v.voter_serial_number DESC LIMIT 1", nativeQuery = true)
    Integer findLastAssignedVoterIdByAdmin(@Param("adminId") Long adminId);


//    @Query("SELECT COUNT(v) FROM Voter v WHERE v.isAssigned = false AND v.admin.id = :adminId")
//    Long countUnassignedVotersByAdmin(@Param("adminId") Long adminId);

    @Query("SELECT v.voterSerialNumber FROM Voter v WHERE v.admin.id = :adminId ORDER BY v.voterSerialNumber DESC LIMIT 1")
    Integer findLastVoterByAdmin_Id(@Param("adminId") Long adminId);

    @Query("SELECT COALESCE(MAX(v.voterSerialNumber), 0) FROM Voter v WHERE v.admin.id = :adminId")
    int getLastSerialNumberByAdmin(@Param("adminId") Long adminId);

    @Query("SELECT v FROM Voter v WHERE v.voterSerialNumber BETWEEN :from AND :to AND v.admin.id = :adminId")
    List<Voter> findBySerialRange(@Param("from") Integer from,
                                  @Param("to") Integer to,
                                  @Param("adminId") Long adminId);

    @Query("""
                SELECT v FROM Voter v
                JOIN v.appUsers u
                WHERE v.admin.id = :adminId
                  AND u.id = :userId
            """)
    Page<Voter> findVotersByAdminIdAndUserId(@Param("adminId") Long adminId,
                                             @Param("userId") Long userId,
                                             Pageable pageable);

    List<Voter> findByAdmin_IdAndIsDeadFalseAndHouseNumberInOrderByHouseNumberAsc(Long userId, List<String> houseNumbers);
}
