package com.example.helmes.controller;

import com.example.helmes.dto.SectorDTO;
import com.example.helmes.dto.UserDTO;
import com.example.helmes.service.SectorService;
import com.example.helmes.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
public class ApiControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectorService sectorService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUserDTO;
    private List<SectorDTO> testSectors;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("John Doe");
        testUserDTO.setSectors(Arrays.asList(1, 2));
        testUserDTO.setAgreeToTerms(true);

        SectorDTO manufacturing = new SectorDTO(1L, "Manufacturing");
        SectorDTO construction = new SectorDTO(2L, "Construction");
        SectorDTO food = new SectorDTO(3L, "Food and Beverage");
        manufacturing.addChild(food);

        testSectors = Arrays.asList(manufacturing, construction);
    }

    @Test
    void testGetSectors() throws Exception {
        when(sectorService.getAllSectorsDTO()).thenReturn(testSectors);

        mockMvc.perform(get("/api/sectors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Manufacturing")))
                .andExpect(jsonPath("$[0].children", hasSize(1)))
                .andExpect(jsonPath("$[0].children[0].name", is("Food and Beverage")))
                .andExpect(jsonPath("$[1].name", is("Construction")))
                .andExpect(jsonPath("$[1].children", hasSize(0)));
    }

    @Test
    void testGetSectorsError() throws Exception {
        when(sectorService.getAllSectorsDTO()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/sectors"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$", is("error: Database error")));
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.sectors", hasSize(2)))
                .andExpect(jsonPath("$.sectors[0]", is(1)))
                .andExpect(jsonPath("$.sectors[1]", is(2)))
                .andExpect(jsonPath("$.agreeToTerms", is(true)));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new NoSuchElementException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error", is("User not found")));
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.saveUser(any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.sectors", hasSize(2)))
                .andExpect(jsonPath("$.sectors[0]", is(1)))
                .andExpect(jsonPath("$.sectors[1]", is(2)))
                .andExpect(jsonPath("$.agreeToTerms", is(true)));
    }

    @Test
    void testCreateUserValidationErrorNameEmpty() throws Exception {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setSectors(Arrays.asList(1, 2));
        invalidUser.setAgreeToTerms(true);
        // Name is missing

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.nameError", is("name empty")));
    }

    @Test
    void testCreateUserValidationErrorSectorsEmpty() throws Exception {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName("John Doe");
        invalidUser.setAgreeToTerms(true);
        // Sectors is missing

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.sectorError", is("sector empty")));
    }

    @Test
    void testCreateUserValidationErrorTermsFalse() throws Exception {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName("John Doe");
        invalidUser.setSectors(Arrays.asList(1, 2));
        invalidUser.setAgreeToTerms(false);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.termsError", is("terms false")));
    }

    @Test
    void testCreateUserMultipleValidationErrors() throws Exception {
        UserDTO invalidUser = new UserDTO();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.nameError", is("name empty")))
                .andExpect(jsonPath("$.sectorError", is("sector empty")))
                .andExpect(jsonPath("$.termsError", is("terms false")));
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.sectors", hasSize(2)))
                .andExpect(jsonPath("$.sectors[0]", is(1)))
                .andExpect(jsonPath("$.sectors[1]", is(2)))
                .andExpect(jsonPath("$.agreeToTerms", is(true)));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        when(userService.updateUser(eq(99L), any(UserDTO.class)))
                .thenThrow(new NoSuchElementException("User not found"));

        mockMvc.perform(post("/api/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error", is("User not found")));
    }

    @Test
    void testUpdateUserValidationErrors() throws Exception {
        UserDTO invalidUser = new UserDTO();

        mockMvc.perform(post("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.nameError", is("name empty")))
                .andExpect(jsonPath("$.sectorError", is("sector empty")))
                .andExpect(jsonPath("$.termsError", is("terms false")));
    }
}
