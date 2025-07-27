package com.example.helmes.controller;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.dto.UserDTO;
import com.example.helmes.service.SectorService;
import com.example.helmes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SectorService sectorService;
    private final UserService userService;

    @Autowired
    public ApiController(SectorService sectorService, UserService userService) {
        this.sectorService = sectorService;
        this.userService = userService;
    }

    @GetMapping("/sectors")
    public ResponseEntity<?> returnSectors() {
        try {
            List<SectorDTO> sectors = sectorService.getAllSectorsDTO();
            return ResponseEntity.ok(sectors);
        } catch (Exception e) {
            System.err.println("error in /get-sectors: " + e.getMessage());
//            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        Map<String, Object> response = new HashMap<>();

        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            response.put("status", "error");
            response.put("nameError", "name empty");
        }

        if (userDTO.getSectors() == null || userDTO.getSectors().isEmpty()) {
            if(!response.containsKey("status")) {
                response.put("status", "error");
            }
            response.put("sectorError", "sector empty");
        }

        if (userDTO.isAgreeToTerms() == null || !userDTO.isAgreeToTerms()) {
            if(!response.containsKey("status")) {
                response.put("status", "error");
            }
            response.put("termsError", "terms false");
        }

        if(!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            UserDTO savedUser = userService.saveUser(userDTO);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("savingError", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        Map<String, Object> response = new HashMap<>();

        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            response.put("status", "error");
            response.put("nameError", "name empty");
        }

        if (userDTO.getSectors() == null || userDTO.getSectors().isEmpty()) {
            if(!response.containsKey("status")) {
                response.put("status", "error");
            }
            response.put("sectorError", "sector empty");
        }

        if (userDTO.isAgreeToTerms() == null || !userDTO.isAgreeToTerms()) {
            if(!response.containsKey("status")) {
                response.put("status", "error");
            }
            response.put("termsError", "terms false");
        }

        if(!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (NoSuchElementException e) {
            response.put("status", "error");
            response.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("savingError", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
