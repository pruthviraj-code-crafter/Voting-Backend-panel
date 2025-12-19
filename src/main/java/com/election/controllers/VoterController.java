package com.election.controllers;


import com.election.modals.requests.VoterRequest;
import com.election.modals.responses.VoterResponse;
import com.election.services.VoterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/voter")
@AllArgsConstructor
@CrossOrigin("*")
public class VoterController {

    private final VoterService voterService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file,
                                             @RequestParam Long adminId,
                                             @RequestParam String wardName,
                                             @RequestParam String wardNumber) {
        long start = System.currentTimeMillis();
        try {
            voterService.save(file, adminId, wardName, wardNumber);
            long end = System.currentTimeMillis();
            return ResponseEntity.ok("✅ Imported successfully in " + (end - start) + " ms");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVoters(@RequestParam String keyword,
                                          @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.searchVoters(keyword, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getVoterById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(voterService.getVoter(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllVoters(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getVoters(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateVoter(@RequestBody VoterRequest voter) {
        Boolean updated = voterService.UpdateVoter(voter);
        if (updated) {
            return ResponseEntity.ok("✅ Voter updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Error: Voter update failed");
        }
    }

    @GetMapping("/get-by-house")
    public ResponseEntity<?> getGroupedVoters(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVotersGroupedByHouseNumber(userId));
    }

    @GetMapping("/get-by-house-v2")
    public ResponseEntity<?> getGroupedVotersV2(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVotersGroupedByHouseNumberV2(userId));
    }

    @PutMapping("/mark-voted")
    public ResponseEntity<?> markVoterAsVoted(@RequestParam Long id) {
        Boolean updated = voterService.makeIsVoted(id);
        if (updated) {
            return ResponseEntity.ok("✅ Voter marked as voted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Error: Failed to mark voter as voted");
        }
    }

    @GetMapping("/get-voted")
    public ResponseEntity<?> getVotedVoters(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getVotedVoter(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-non-voted")
    public ResponseEntity<?> getNonVotedVoters(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getNoneVotedVoter(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-with-mobile")
    public ResponseEntity<?> getVotersWithMobile(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getVoterWithMobile(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-without-mobile")
    public ResponseEntity<?> getVotersWithoutMobile(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getVoterWithNoMobile(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-dead-voters")
    public ResponseEntity<?> getDeadVoters(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(voterService.getDeadVoter(page, size, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-religion")
    public ResponseEntity<?> getVotersGroupedByReligion(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVotersGroupedByReligion(userId));
    }

    @GetMapping("/by-age-range")
    public ResponseEntity<?> getVotersByAgeRange(@RequestParam Long userId,
                                                 @RequestParam int startAge,
                                                 @RequestParam int endAge) {
        try {
            List<VoterResponse> voters = voterService.getVotersByAgeRange(userId, startAge, endAge);
            return ResponseEntity.ok(voters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-cast")
    public ResponseEntity<?> getVotersGroupedByCast(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVotersGroupedByCast(userId));
    }

    @GetMapping("/get-by-voter-status")
    public ResponseEntity<?> getVotersGroupedByStatus(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVotersGroupedByStatus(userId));
    }

    @GetMapping("/get-committee")
    public ResponseEntity<?> getVotersGroupedByEducation(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getBoothCommittee(userId));
    }

    @GetMapping("/get-unassigned-voters")
    public ResponseEntity<?> getUnassignedVoterIdsByAdmin(@RequestParam Long userId) {
        try {
            List<Long> voterIds = voterService.getUnassignedVoterIdsByAdmin(userId);
            return ResponseEntity.ok(voterIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-last-assigned-voter")
    public ResponseEntity<?> getLastAssignedVoterIdByAdmin(@RequestParam Long userId) {
        Integer voterId = voterService.getLastAssignedVoterIdsByAdmin(userId) + 1;
        return ResponseEntity.ok(voterId);
    }

    @GetMapping("/count-unassigned-voters")
    public ResponseEntity<?> countUnassignedVotersByAdmin(@RequestParam Long userId) {
        Integer count = voterService.countUnassignedVotersByAdmin(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/search-public")
    public ResponseEntity<?> searchPublicVoters(@RequestParam String keyword) {
        System.out.println("Public search keyword: " + keyword);
        try {
            return ResponseEntity.ok(voterService.searchPublicVoters(keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-voter-report")
    public ResponseEntity<?> getVoterReport(@RequestParam Long userId) {
        return ResponseEntity.ok(voterService.getVoterReport(userId));
    }

}
