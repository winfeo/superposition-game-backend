package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.dto.fromApp.NewUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;
import io.github.winfeo.superpositiongame.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService; //TODO сделать AuthService?

    @PostMapping("/register")
    public ResponseEntity<UserDTO> createUser(@RequestBody NewUserDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(dto));
    }

//    @GetMapping("/login")
//    public ResponseEntity<UserDTO> loginUser(Authentication authentication) {
//        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(authentication.getName()));
//    }

    @PostMapping("/login") //TODO переделать
    public ResponseEntity<UserDTO> loginUser(@RequestBody NewUserDTO dto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserByEmail(dto.getEmail()));
    }
}
