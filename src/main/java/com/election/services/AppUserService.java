package com.election.services;

import com.election.modals.requests.AdminRequest;
import com.election.modals.requests.UserRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.responses.UserResponse;
import com.election.modals.responses.VoterResponse;

import java.util.List;

public interface AppUserService {

    Boolean createUser(UserRequest user);

    Boolean updateUser(UserRequest user);

    UserResponse getUserByUsername(String username);

    UserResponse getById(Long id);

    List<UserResponse> getAllUsers(Long adminId);

    List<UserResponse> getUsersByAdmin(Long adminId);

    Boolean deleteUser(Long id);

    PaginatedResponse<VoterResponse> getVotersByUser(Long userId, int page, int size);

    Object getVotersByAppUser(Long appUserId);
}
