package com.example.helmes.service;

import com.example.helmes.dto.UserDTO;
import com.example.helmes.model.User;
import com.example.helmes.model.Sector;
import com.example.helmes.repository.UserRepository;
import com.example.helmes.repository.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Sector sector1, sector2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sector1 = new Sector(1L, "Manufacturing", null);
        sector2 = new Sector(2L, "Construction", null);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setSectors(Arrays.asList(sector1, sector2));
        testUser.setAgreeToTerms(true);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("John Doe");
        testUserDTO.setSectors(Arrays.asList(1, 2));
        testUserDTO.setAgreeToTerms(true);
    }

    @Test
    void testSaveUser() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector1));
        when(sectorRepository.findById(2L)).thenReturn(Optional.of(sector2));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.saveUser(testUserDTO);

        verify(sectorRepository, times(1)).findById(1L);
        verify(sectorRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(any(User.class));

        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getName(), result.getName());
        assertEquals(testUserDTO.getSectors().size(), result.getSectors().size());
        assertTrue(result.isAgreeToTerms());
    }

    @Test
    void testUpdateUser() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setName("Jane Doe");
        updatedUserDTO.setSectors(Arrays.asList(1));
        updatedUserDTO.setAgreeToTerms(false);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Jane Doe");
        updatedUser.setSectors(Arrays.asList(sector1));
        updatedUser.setAgreeToTerms(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector1));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("Jane Doe", capturedUser.getName());
        assertEquals(1, capturedUser.getSectors().size());
        assertEquals(1L, capturedUser.getSectors().get(0).getId());
        assertFalse(capturedUser.isAgreeToTerms());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Doe", result.getName());
        assertEquals(1, result.getSectors().size());
        assertEquals(1, result.getSectors().get(0));
        assertFalse(result.isAgreeToTerms());
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
            userService.updateUser(99L, testUserDTO)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserWithInvalidSector() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setName("Jane Doe");
        updatedUserDTO.setSectors(Arrays.asList(999));
        updatedUserDTO.setAgreeToTerms(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(1L, updatedUserDTO)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        verify(userRepository, times(1)).findById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(2, result.getSectors().size());
        assertTrue(result.getSectors().contains(1));
        assertTrue(result.getSectors().contains(2));
        assertTrue(result.isAgreeToTerms());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
            userService.getUserById(99L)
        );
    }

    @Test
    void testSaveUserWithInvalidSector() {
        UserDTO userDTOWithInvalidSector = new UserDTO();
        userDTOWithInvalidSector.setName("John Doe");
        userDTOWithInvalidSector.setSectors(Arrays.asList(1, 999));
        userDTOWithInvalidSector.setAgreeToTerms(true);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector1));
        when(sectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            userService.saveUser(userDTOWithInvalidSector)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
