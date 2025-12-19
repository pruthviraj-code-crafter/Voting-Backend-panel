package com.election.modals.responses;

import lombok.Data;

@Data
public class VoterReportDto {
    private Integer totalVoters;
    private Integer totalMales;
    private Integer totalFemales;
    private Integer totalOthers;
}
