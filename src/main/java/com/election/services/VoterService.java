package com.election.services;

import com.election.modals.requests.VoterRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.Voter;
import com.election.modals.responses.VoterReportDto;
import com.election.modals.responses.VoterResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface VoterService {

    void save(MultipartFile file,Long adminId, String wardName , String wardNumber ) throws Exception;

    List<VoterResponse> searchVoters(String keyword,Long userId);

    List<VoterResponse> searchPublicVoters(String keyword);

    VoterResponse getVoter(Long id);

    PaginatedResponse<VoterResponse> getVoters(int page , int size , Long id);

    Boolean UpdateVoter(VoterRequest voter);

    Map<String, List<VoterResponse>> getVotersGroupedByHouseNumber(Long userId);

    Map<String, List<VoterResponse>> getVotersGroupedByHouseNumberV2(Long userId);

    Boolean makeIsVoted(Long id);

    PaginatedResponse<VoterResponse> getVotedVoter(int page , int size , Long id);

    PaginatedResponse<VoterResponse> getNoneVotedVoter(int page , int size , Long id);

    PaginatedResponse<VoterResponse> getVoterWithMobile(int page , int size , Long id);

    PaginatedResponse<VoterResponse> getVoterWithNoMobile(int page , int size , Long id);

    PaginatedResponse<VoterResponse> getDeadVoter(int page , int size , Long id);

    Map<String, List<VoterResponse>> getVotersGroupedByReligion(Long userId);

    Map<String, List<VoterResponse>> getVotersGroupedByCast(Long userId);

    Map<String, List<VoterResponse>> getVotersGroupedByStatus(Long userId);

    Map<String , List<VoterResponse>> getBoothCommittee(Long userId);

    List<VoterResponse> getVotersByAgeRange(Long userId, int startAge, int endAge);

    List<Long> getUnassignedVoterIdsByAdmin(Long adminId);

    Integer getLastAssignedVoterIdsByAdmin(Long adminId);

    Integer countUnassignedVotersByAdmin(Long adminId);

    VoterReportDto getVoterReport(Long adminId);
}
