package com.election.services;

import com.election.modals.requests.AdminRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.responses.VoterResponse;
import org.apache.catalina.User;

import java.util.List;

public interface AppAdminService {

    Boolean addUser(AdminRequest user);

    Boolean updateUser(AdminRequest user);

    User getUserByUsername(String username);

    Boolean deleteUser(Long id);

    Boolean enableUser(Long id);

    Boolean disableUser(Long id);

    List<AdminRequest> getUsers();

    AdminRequest getUserById(Long id);

    PaginatedResponse<VoterResponse> getVoterByAdminAndUser(Long adminId , Long userId, int page, int pageSize);

}
