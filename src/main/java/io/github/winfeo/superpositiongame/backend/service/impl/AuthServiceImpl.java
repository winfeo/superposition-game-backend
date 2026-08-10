package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;
import io.github.winfeo.superpositiongame.backend.jwt.AuthRequestDTO;
import io.github.winfeo.superpositiongame.backend.jwt.AuthResponseDTO;
import io.github.winfeo.superpositiongame.backend.jwt.JwtUtil;
import io.github.winfeo.superpositiongame.backend.service.AuthService;
import io.github.winfeo.superpositiongame.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public AuthResponseDTO authenticateAndGenerateToken(AuthRequestDTO authRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        authRequest.getEmail(),
//                        authRequest.getPassword()
//                )
//        );
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String token = jwtUtil.generateToken(userDetails.getUsername());
//        UserDTO dto = userService.getUserByEmail(userDetails.getUsername());
//
//        return new AuthResponseDTO(token, dto);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            UserDTO dto = userService.getUserByEmail(userDetails.getUsername());

            return new AuthResponseDTO(token, dto);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неверный email или пароль");
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e.getMessage()) {};
        }
    }
}
