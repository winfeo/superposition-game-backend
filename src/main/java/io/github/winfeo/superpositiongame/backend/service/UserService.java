package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.dto.fromApp.NewUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.fromApp.UpdateUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO getUserByNickname(String nickname);
    UserDTO getUserByEmail(String email);
    UserDTO createUser(NewUserDTO dto);
    UserDTO updateUser(UpdateUserDTO dto);
    void deleteUser(Long id);
}
