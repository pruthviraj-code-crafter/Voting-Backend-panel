package com.election.services.impls;

import com.election.modals.AppAdmin;
import com.election.modals.AppUser;
import com.election.modals.Ward;
import com.election.modals.requests.AdminRequest;
import com.election.modals.requests.VoterRequest;
import com.election.modals.responses.PaginatedResponse;
import com.election.modals.Voter;
import com.election.modals.responses.VoterReportDto;
import com.election.modals.responses.VoterResponse;
import com.election.modals.responses.WardResponse;
import com.election.repositories.AppAdminRepository;
import com.election.repositories.AppUserRepository;
import com.election.repositories.VoterRepository;
import com.election.repositories.WardRepository;
import com.election.services.VoterService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class VoterServiceImpl implements VoterService {

    private final VoterRepository voterRepository;
    private final PaginationResponseImpl paginationResponse;
    private final AppAdminRepository userRepository;
    private final AppUserRepository appUserRepository;
    private final WardRepository wardRepository;

    @Transactional
    @Override
    public void save(MultipartFile file, Long adminId, String wardName, String wardNumber) throws Exception {

        Optional<AppAdmin> appAdmin = userRepository.findById(adminId);

        Ward savedWard;

        // Check if ward exists for admin, otherwise create
        Optional<Ward> existingWard = wardRepository.findByWardNameAndUserId(wardName.trim(), adminId);

        if (existingWard.isPresent()) {
            savedWard = existingWard.get();
        } else {
            Ward ward = new Ward();
            ward.setWardName(wardName.trim());
            ward.setWardNumber(wardNumber);
            ward.setUser(appAdmin.get());
            savedWard = wardRepository.save(ward);
        }

        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty!");
        }

        // --- 🔥 Get last serial before parsing CSV (performance optimized)
        int nextSerial = voterRepository.getLastSerialNumberByAdmin(adminId) + 1;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {

            CSVFormat format = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .withAllowMissingColumnNames();

            try (CSVParser csvParser = new CSVParser(reader, format)) {

                Map<String, Integer> headers = new HashMap<>();
                csvParser.getHeaderMap().forEach((key, value) -> {
                    String cleanKey = key.replace("\uFEFF", "").trim().toLowerCase();
                    headers.put(cleanKey, value);
                });

                List<Voter> users = new ArrayList<>();

                for (CSVRecord record : csvParser) {
                    if (record.size() == 0) continue;

                    Voter voter = new Voter();

                    voter.setEpic(getValue(record, headers, "epic"));
                    voter.setZoneNumber(getValue(record, headers, "zonenumber"));
                    voter.setVoterName(getValue(record, headers, "votername"));
                    voter.setMiddleName(getValue(record, headers, "middlename"));
                    voter.setHouseNumber(getValue(record, headers, "housenumber"));
                    voter.setAge(getValue(record, headers, "age"));
                    voter.setGender(getValue(record, headers, "gender"));
                    voter.setVoterNumber(Integer.parseInt(getValue(record, headers, "voternumber")));

                    voter.setAdmin(appAdmin.get());
                    voter.setWard(savedWard);

                    // --- 🔥 Assign next voter serial number
                    voter.setVoterSerialNumber(nextSerial++);

                    // Skip blank rows
                    if (voter.getVoterNumber() == null &&
                            voter.getMiddleName() == null &&
                            voter.getHouseNumber() == null &&
                            voter.getEpic() == null &&
                            voter.getZoneNumber() == null) {
                        continue;
                    }

                    users.add(voter);
                }

                if (!users.isEmpty()) {
                    voterRepository.saveAll(users);
                } else {
                    throw new RuntimeException("No valid data found in the CSV file!");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to import CSV file. Please check encoding (UTF-8) and format.", e);
        }
    }

    /**
     * Safe get value using cleaned header map.
     */
    private String getValue(CSVRecord record, Map<String, Integer> headers, String columnName) {
        Integer index = headers.get(columnName.toLowerCase());
        if (index == null) return "";
        String value = record.get(index);
        return value != null ? value.trim() : "";
    }

    @Override
    public List<VoterResponse> searchVoters(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) {
            return Collections.emptyList();
        }

        Specification<Voter> spec = (root, query, cb) -> {
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.and(
                    cb.equal(root.get("admin").get("id"), userId), // Filter by user
                    cb.or(
                            cb.like(cb.lower(root.get("epic")), likePattern),
                            cb.like(cb.lower(root.get("voterName")), likePattern),
                            cb.like(cb.lower(root.get("middleName")), likePattern),
                            cb.like(cb.lower(root.get("houseNumber")), likePattern),
                            cb.like(cb.lower(root.get("number")), likePattern),
                            cb.like(cb.lower(root.get("caste")), likePattern),
                            cb.like(cb.lower(root.get("religion")), likePattern)
                    )
            );
        };
        return voterRepository.findAll(spec)
                .stream()
                .map(this::voterResponse)
                .toList();
    }


    @Override
    public List<VoterResponse> searchPublicVoters(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Specification<Voter> spec = (root, query, cb) -> {
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("epic")), likePattern),
                    cb.like(cb.lower(root.get("voterName")), likePattern),
                    cb.like(cb.lower(root.get("middleName")), likePattern),
                    cb.like(cb.lower(root.get("number")), likePattern)
            );
        };

        return voterRepository.findAll(spec)
                .stream()
                .map(this::voterResponse)
                .toList();
    }


    @Override
    public VoterResponse getVoter(Long id) {
        return voterResponse(voterRepository.findById(id).isPresent() ? voterRepository.findById(id).get() : null);
    }

    @Override
    public PaginatedResponse<VoterResponse> getVoters(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findAllByAdmin_Id(id, pageable);
        List<VoterResponse> list = voters.getContent().stream()
                .map(this::voterResponse)
                .toList();
        return paginationResponse.buildPaginatedResponse(list, voters);
    }

    /**
     * Map Voter to VoterResponse
     *
     * @param voter
     * @return
     */
    public VoterResponse voterResponse(Voter voter) {
        VoterResponse voterResponse = new VoterResponse();
        voterResponse.setId(voter.getId());
        voterResponse.setEpic(voter.getEpic());
        voterResponse.setZoneNumber(voter.getZoneNumber());
        voterResponse.setVoterName(voter.getVoterName());
        voterResponse.setMiddleName(voter.getMiddleName());
        voterResponse.setHouseNumber(voter.getHouseNumber());
        voterResponse.setAge(voter.getAge());
        voterResponse.setGender(voter.getGender());
        voterResponse.setVoterNumber(voter.getVoterNumber());
        voterResponse.setNumber(voter.getNumber());
        voterResponse.setBirthdate(voter.getBirthdate());
        voterResponse.setProfession(voter.getProfession());
        voterResponse.setReligion(voter.getReligion());
        voterResponse.setCaste(voter.getCaste());
        voterResponse.setNumber(voter.getNumber());
        voterResponse.setVoterStatus(voter.getVoterStatus());
        voterResponse.setBoothCommittee(voter.getBoothCommittee());
        voterResponse.setBoothNumber(voter.getBoothNumber());
        voterResponse.setDesignation(voter.getDesignation());
        voterResponse.setRemark(voter.getRemark());
        voterResponse.setIsDead(voter.getIsDead());
        voterResponse.setIsVoted(voter.getIsVoted());
        voterResponse.setVoterSerialNumber(voter.getVoterSerialNumber());

        WardResponse wardResponse = new WardResponse();
        if (voter.getWard() != null) {
            wardResponse.setId(voter.getWard().getId());
            wardResponse.setWardName(voter.getWard().getWardName());
            wardResponse.setWardNumber(voter.getWard().getWardNumber());
            voterResponse.setWard(wardResponse);
        }
        return voterResponse;
    }

    @Override
    public Boolean UpdateVoter(VoterRequest voter) {
        Optional<Voter> optionalVoter = voterRepository.findById(voter.getId());
        if (optionalVoter.isPresent()) {
            Voter existingVoter = optionalVoter.get();
            existingVoter.setEpic(voter.getEpic());
            existingVoter.setZoneNumber(voter.getZoneNumber());
            existingVoter.setVoterName(voter.getVoterName());
            existingVoter.setMiddleName(voter.getMiddleName());
            existingVoter.setHouseNumber(voter.getHouseNumber());
            existingVoter.setAge(voter.getAge());
            existingVoter.setGender(voter.getGender());
            existingVoter.setVoterNumber(voter.getVoterNumber());
            existingVoter.setBirthdate(voter.getBirthdate());
            existingVoter.setProfession(voter.getProfession());
            existingVoter.setIsDead(voter.getIsDead());
            existingVoter.setReligion(voter.getReligion());
            existingVoter.setCaste(voter.getCaste());
            existingVoter.setNumber(voter.getNumber());
            existingVoter.setVoterStatus(voter.getVoterStatus());
            existingVoter.setBoothCommittee(voter.getBoothCommittee());
            existingVoter.setBoothNumber(voter.getBoothNumber());
            existingVoter.setDesignation(voter.getDesignation());
            existingVoter.setRemark(voter.getRemark());
            voterRepository.save(existingVoter);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, List<VoterResponse>> getVotersGroupedByHouseNumber(Long userId) {
        List<Voter> voters = voterRepository.findByIsDeadFalseOrderByHouseNumberAsc();

        // ✅ Filter, group, and convert to VoterResponse
        return voters.stream()
                // Only voters belonging to this user
                .filter(v -> v.getAdmin() != null && v.getAdmin().getId().equals(userId))

                // Exclude invalid house numbers
                .filter(v -> {
                    String houseNo = v.getHouseNumber();
                    return houseNo != null && !houseNo.trim().isEmpty() && !houseNo.trim().equals("-");
                })

                // Group by valid house number
                .collect(Collectors.groupingBy(
                        Voter::getHouseNumber,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))

                // Keep only houses with more than one voter
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)

                // Convert each voter list → list of VoterResponse
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::voterResponse)
                                .collect(Collectors.toList()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, List<VoterResponse>> getVotersGroupedByHouseNumberV2(Long userId) {
        // Step 1: find only house numbers with more than 1 voter
        List<String> houseNumbers = voterRepository.findHouseNumbersHavingMultipleVoters(userId);

        if (houseNumbers.isEmpty()) return Collections.emptyMap();

        // Step 2: fetch only voters in those houses (very fast)
        List<Voter> voters =
                voterRepository.findByAdmin_IdAndIsDeadFalseAndHouseNumberInOrderByHouseNumberAsc(
                        userId, houseNumbers);

        // Step 3: group & map
        return voters.stream()
                .collect(Collectors.groupingBy(
                        Voter::getHouseNumber,
                        LinkedHashMap::new,
                        Collectors.mapping(this::voterResponse, Collectors.toList())
                ));
    }

    @Override
    public Boolean makeIsVoted(Long id) {
        Optional<Voter> optionalVoter = voterRepository.findById(id);
        if (optionalVoter.isPresent()) {
            optionalVoter.get().setIsVoted(true);
            voterRepository.save(optionalVoter.get());
            return true;
        }
        return false;
    }

    @Override
    public PaginatedResponse<VoterResponse> getVotedVoter(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findByIsVotedTrueAndAdmin_Id(id, pageable);
        List<VoterResponse> list = voters.getContent().stream()
                .map(this::voterResponse)
                .toList();
        return paginationResponse.buildPaginatedResponse(list, voters);
    }

    @Override
    public PaginatedResponse<VoterResponse> getNoneVotedVoter(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findByIsVotedFalseAndAdmin_Id(id, pageable);
        List<VoterResponse> voterResponses = voters.getContent().stream()
                .map(this::voterResponse)
                .toList();
        return paginationResponse.buildPaginatedResponse(voterResponses, voters);
    }

    @Override
    public PaginatedResponse<VoterResponse> getVoterWithMobile(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findAllByAdmin_IdAndNumberIsNotNullAndNumberNot(id, "", pageable);
        List<VoterResponse> list = voters.getContent().stream().map(this::voterResponse).toList();
        return paginationResponse.buildPaginatedResponse(list, voters);
    }

    @Override
    public PaginatedResponse<VoterResponse> getVoterWithNoMobile(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findAllByUser_IdAndNumberIsNullOrEmpty(id, pageable);
        List<VoterResponse> list = voters.getContent().stream().map(this::voterResponse).toList();
        return paginationResponse.buildPaginatedResponse(list, voters);
    }

    @Override
    public Map<String, List<VoterResponse>> getVotersGroupedByReligion(Long userId) {
        List<Voter> voters = voterRepository.findAllByAdmin_IdAndIsDeadFalse(userId);

        // Group by religion (handle nulls properly)
        return voters.stream()
                .filter(v -> v.getReligion() != null && !v.getReligion().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        v -> v.getReligion().trim(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::voterResponse, Collectors.toList())
                ));
    }

    @Override
    public Map<String, List<VoterResponse>> getVotersGroupedByCast(Long userId) {
        List<Voter> voters = voterRepository.findAllByAdmin_IdAndIsDeadFalse(userId);

        // Group by religion (handle nulls properly)
        return voters.stream()
                .filter(v -> v.getCaste() != null && !v.getCaste().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        v -> v.getCaste().trim(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::voterResponse, Collectors.toList())
                ));
    }

    @Override
    public Map<String, List<VoterResponse>> getVotersGroupedByStatus(Long userId) {
        List<Voter> voters = voterRepository.findAllByAdmin_IdAndIsDeadFalse(userId);

        return voters.stream()
                .filter(v -> v.getVoterStatus() != null && !v.getVoterStatus().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        v -> v.getVoterStatus().trim().toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::voterResponse, Collectors.toList())
                ));
    }

    @Override
    public Map<String, List<VoterResponse>> getBoothCommittee(Long userId) {
        List<Voter> voters = voterRepository.findAllByAdmin_IdAndIsDeadFalse(userId);

        return voters.stream()
                .filter(v -> v.getBoothCommittee() != null && !v.getBoothCommittee().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        v -> v.getBoothCommittee().trim().toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::voterResponse, Collectors.toList())
                ));
    }

    @Override
    public PaginatedResponse<VoterResponse> getDeadVoter(int page, int size, Long id) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voter> voters = voterRepository.findByIsDeadTrueAndAdmin_Id(id, pageable);
        List<VoterResponse> list = voters.getContent().stream().map(this::voterResponse).toList();
        return paginationResponse.buildPaginatedResponse(list, voters);
    }

    @Override
    public List<VoterResponse> getVotersByAgeRange(Long userId, int startAge, int endAge) {
        List<Voter> voters = voterRepository.findAllByAdmin_IdAndIsDeadFalse(userId);

        return voters.stream()
                .filter(v -> {
                    try {
                        int age = Integer.parseInt(v.getAge());
                        return age >= startAge && age <= endAge;
                    } catch (NumberFormatException e) {
                        return false; // skip invalid or non-numeric ages
                    }
                }).map(this::voterResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUnassignedVoterIdsByAdmin(Long adminId) {
        return voterRepository.findUnassignedVoterIdsByAdmin(adminId);
    }

    @Override
    public Integer getLastAssignedVoterIdsByAdmin(Long adminId) {
        Integer voterIdByAdmin = voterRepository.findLastAssignedVoterIdByAdmin(adminId);
        return voterIdByAdmin != null ? voterIdByAdmin : 0;
    }

    @Override
    public Integer countUnassignedVotersByAdmin(Long adminId) {
        return voterRepository.findLastVoterByAdmin_Id(adminId);
    }

    @Override
    public VoterReportDto getVoterReport(Long adminId) {
        List<Voter> byAdminId = voterRepository.findByAdmin_Id(adminId);
        int totalMales = 0;
        int totalFemales = 0;
        int totalOthers = 0;

        for (Voter voter : byAdminId) {
            if ("".equals(voter.getVoterStatus())) {
                continue;
            } else if ("पु".equals(voter.getGender())) {
                totalMales++;
            } else if ("स्त्री".equals(voter.getGender())) {
                totalFemales++;
            } else {
                totalOthers++;
            }
        }

        VoterReportDto voterReportDto = new VoterReportDto();
        voterReportDto.setTotalVoters(byAdminId.size());
        voterReportDto.setTotalFemales(totalFemales);
        voterReportDto.setTotalMales(totalMales);
        voterReportDto.setTotalOthers(totalOthers);

        return voterReportDto;
    }
}
