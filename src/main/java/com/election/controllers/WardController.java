package com.election.controllers;

import com.election.services.WardService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ward")
@AllArgsConstructor
@CrossOrigin("*")
public class WardController {

    private final WardService wardService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllWards() {
        try {
            return ResponseEntity.ok(wardService.getAllWards());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }


}
