package com.example.helmes.service;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.model.Sector;
import com.example.helmes.repository.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SectorServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private SectorService sectorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllSectorsDTOWithEmptyList() {
        when(sectorRepository.findAll()).thenReturn(new ArrayList<>());

        List<SectorDTO> result = sectorService.getAllSectorsDTO();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sectorRepository, times(1)).findAll();
    }

    @Test
    void testGetAllSectorsDTO() {
        Sector manufacturing = new Sector(1L, "Manufacturing", null);

        Sector printing = new Sector(2L, "Printing", manufacturing);
        Sector food = new Sector(3L, "Food and Beverage", manufacturing);

        Sector labelling = new Sector(4L, "Labelling", printing);

        List<Sector> allSectors = List.of(manufacturing, printing, food, labelling);

        when(sectorRepository.findAll()).thenReturn(allSectors);

        List<SectorDTO> result = sectorService.getAllSectorsDTO();

        assertNotNull(result);
        assertEquals(1, result.size()); // Only root sector (manufacturing)
        assertEquals("Manufacturing", result.get(0).getName());

        List<SectorDTO> manufacturingChildren = result.get(0).getChildren();
        assertEquals(2, manufacturingChildren.size());

        SectorDTO printingDTO = manufacturingChildren.stream()
                .filter(dto -> dto.getName().equals("Printing"))
                .findFirst()
                .orElse(null);

        assertNotNull(printingDTO);
        assertEquals(1, printingDTO.getChildren().size());
        assertEquals("Labelling", printingDTO.getChildren().get(0).getName());

        SectorDTO foodDTO = manufacturingChildren.stream()
                .filter(dto -> dto.getName().equals("Food and Beverage"))
                .findFirst()
                .orElse(null);

        assertNotNull(foodDTO);
        assertTrue(foodDTO.getChildren().isEmpty());
    }

    @Test
    void testGetAllSectorsDTOSorting() {
        // Setup sectors with names that test the sorting
        Sector manufacturing = new Sector(1L, "Manufacturing", null);
        Sector other = new Sector(2L, "Other", null);
        Sector agriculture = new Sector(3L, "Agriculture", null);

        List<Sector> allSectors = List.of(manufacturing, other, agriculture);

        when(sectorRepository.findAll()).thenReturn(allSectors);

        List<SectorDTO> result = sectorService.getAllSectorsDTO();

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals("Agriculture", result.get(0).getName());
        assertEquals("Manufacturing", result.get(1).getName());
        assertEquals("Other", result.get(2).getName());
    }
}
