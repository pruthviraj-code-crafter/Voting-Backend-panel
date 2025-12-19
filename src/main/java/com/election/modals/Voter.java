package com.election.modals;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "voter")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Voter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Boolean isDead = false;
    private Boolean isVoted = false;
    private Boolean isAssigned = false;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private AppAdmin admin;

    @ManyToMany(mappedBy = "voters")
    private List<AppUser> appUsers = new ArrayList<>();

}