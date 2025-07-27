package com.example.helmes.controller;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.service.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SectorService sectorService;

    @Autowired
    public ApiController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @GetMapping("/test")
    public String tere() {
        return "tere!";
    }

    @GetMapping("/get-sectors")
    public ResponseEntity<?> returnSectors() {
        try {
            List<SectorDTO> sectors = sectorService.getAllSectorsDTO();
            return ResponseEntity.ok(sectors);
        } catch (Exception e) {
            System.err.println("error in /get-sectors: " + e.getMessage());
//            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error when getting sectors: " + e.getMessage());
        }
    }
}
