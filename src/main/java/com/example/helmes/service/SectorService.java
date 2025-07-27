package com.example.helmes.service;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.model.Sector;
import com.example.helmes.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    @Autowired
    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public List<SectorDTO> getAllSectorsDTO() {
        List<Sector> allSectors = sectorRepository.findAll();

        Map<Long, SectorDTO> sectorDTOMap = new HashMap<>();

        for (Sector sector : allSectors) {
            SectorDTO dto = new SectorDTO(sector.getId(), sector.getName());
            sectorDTOMap.put(sector.getId(), dto);
        }

        List<SectorDTO> rootSectors = new ArrayList<>();

        for (Sector sector : allSectors) {
            SectorDTO currentDTO = sectorDTOMap.get(sector.getId());

            if (sector.getParent() == null) {
                rootSectors.add(currentDTO);
            } else {
                SectorDTO parentDTO = sectorDTOMap.get(sector.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.addChild(currentDTO);
                }
            }
        }

        return rootSectors;
    }
}
