package com.election.services.impls;

import com.election.modals.Ward;
import com.election.repositories.WardRepository;
import com.election.services.WardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WardServiceImpl implements WardService {

    private final WardRepository wardRepository;

    @Override
    public List<Ward> getAllWards() {
        return wardRepository.findAll();
    }
}
