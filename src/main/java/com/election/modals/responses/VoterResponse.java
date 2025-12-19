package com.election.modals.responses;

import com.election.modals.requests.AdminRequest;
import lombok.Data;

@Data
public class VoterResponse {
    private Long id;
    private String epic;
    private String zoneNumber;
    private String voterName;
    private String middleName;
    private String houseNumber;
    private String age;
    private String gender;
    private Integer voterNumber;
    private Integer voterSerialNumber;
    private String number;
    private String birthdate;
    private String profession;
    private String religion;
    private String caste;
    private String voterStatus;
    private String boothCommittee;
    private String boothNumber;
    private String designation;
    private String remark;
    private Boolean isDead ;
    private Boolean isVoted;
    private WardResponse ward;
    private AdminRequest user;
}
