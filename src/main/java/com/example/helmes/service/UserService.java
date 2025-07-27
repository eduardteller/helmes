package com.example.helmes.service;

import com.example.helmes.dto.UserDTO;
import com.example.helmes.model.User;
import com.example.helmes.model.Sector;
import com.example.helmes.repository.UserRepository;
import com.example.helmes.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;

    @Autowired
    public UserService(UserRepository userRepository, SectorRepository sectorRepository) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
    }

    // save new user
    public UserDTO saveUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // update existing user by id
    public UserDTO updateUser(Long id, UserDTO userDTO) {

        // find existing user by id, if not found, throw exception
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("not found: " + id));

        // update user fields
        existingUser.setName(userDTO.getName());
        existingUser.setAgreeToTerms(userDTO.isAgreeToTerms());

        List<Sector> sectors = userDTO.getSectors().stream()
                .map(sectorId -> sectorRepository.findById(sectorId.longValue())
                        .orElseThrow(() -> new IllegalArgumentException("sector err: " + sectorId)))
                .collect(Collectors.toList());
        existingUser.setSectors(sectors);

        // save updated user and convert to DTO and return
        User savedUser = userRepository.save(existingUser);
        return convertToDTO(savedUser);
    }

    // get user by id and convert to UserDTO and return
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return convertToDTO(user);
    }

    // convert UserDTO to User
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        List<Sector> sectors = userDTO.getSectors().stream()
                .map(sectorId -> sectorRepository.findById(sectorId.longValue())
                        .orElseThrow(() -> new IllegalArgumentException("not found: " + sectorId)))
                .collect(Collectors.toList());
        user.setSectors(sectors);
        user.setAgreeToTerms(userDTO.isAgreeToTerms());
        return user;
    }

    // convert User to UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        List<Integer> sectorIds = user.getSectors().stream()
                .map(sector -> sector.getId().intValue())
                .collect(Collectors.toList());
        userDTO.setSectors(sectorIds);
        userDTO.setAgreeToTerms(user.isAgreeToTerms());
        return userDTO;
    }
}
