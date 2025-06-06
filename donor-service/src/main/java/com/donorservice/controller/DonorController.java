package com.donorservice.controller;

import com.donorservice.aop.RequireRole;
import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.service.DonorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final UserClient userClient;
    private final DonorService donorService;

    public DonorController(UserClient userClient, DonorService donorService) {
        this.userClient = userClient;
        this.donorService = donorService;
    }
    @PutMapping("/addRole")
    public ResponseEntity<String> addRoleToUser(@RequestHeader("id") String userId) {
        userClient.addRole(UUID.fromString(userId), "DONOR");
        return ResponseEntity.ok("Role added");
    }

    @RequireRole("DONOR")
    @PostMapping("/register")
    public ResponseEntity<DonorDTO> registerDonor(@RequestHeader("id") UUID userId, @RequestBody RegisterDonor donorDTO) {
        return ResponseEntity.ok(donorService.createDonor(userId, donorDTO));
    }

    @RequireRole("DONOR")
    @PostMapping("/donate")
    public ResponseEntity<DonationDTO> registerDonation(@RequestBody DonationRequestDTO donationDTO) {
        DonationDTO response = donorService.registerDonation(donationDTO);
        return ResponseEntity.ok(response);
    }

    @RequireRole("DONOR")
    @GetMapping("/by-userId")
    public ResponseEntity<DonorDTO> getDonorByUserId(@RequestHeader("id") UUID userId) {
        DonorDTO donor = donorService.getDonorByUserId(userId);
        if (donor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donor);
    }


    @RequireRole("DONOR")
    @GetMapping("/{id}")
    public DonorDTO getDonor(@PathVariable UUID id) {
        return donorService.getDonorById(id);
    }

    @RequireRole("DONOR")
    @GetMapping("/{donorId}/donations")
    public ResponseEntity<List<DonationDTO>> getDonations(@PathVariable UUID donorId) {
        return ResponseEntity.ok(donorService.getDonationsByDonorId(donorId));
    }
}
