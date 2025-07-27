package com.example.helmes.service;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.model.Sector;
import com.example.helmes.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    @Autowired
    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public List<SectorDTO> getAllSectorsDTO() {
        // get all sectors from the db
        List<Sector> allSectors = sectorRepository.findAll();

        // sort sectors by name alphabetically
        allSectors = allSectors.stream()
                .sorted(Comparator.comparing(Sector::getName))
                .toList();

        Map<Long, SectorDTO> sectorDTOMap = new HashMap<>();

        // create a map of SectorDTOs
        for (Sector sector : allSectors) {
            SectorDTO dto = new SectorDTO(sector.getId(), sector.getName());
            sectorDTOMap.put(sector.getId(), dto);
        }

        List<SectorDTO> rootSectors = new ArrayList<>();

        // build the recursive structure where each sector has a list of children as SectorDTOs
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

        // put "Other" level 0 parent at the end of the list
//        rootSectors.sort((a, b) -> {
//            if ("Other".equals(a.getName())) return 1;
//            if ("Other".equals(b.getName())) return -1;
//            return a.getName().compareTo(b.getName());
//        });

        return rootSectors;
    }
}
