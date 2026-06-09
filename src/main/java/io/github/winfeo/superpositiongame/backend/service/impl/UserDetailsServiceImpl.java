package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.entity.db.AuthData;
import io.github.winfeo.superpositiongame.backend.exception.UserNotFoundException;
import io.github.winfeo.superpositiongame.backend.repository.AuthDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AuthDataRepository authDataRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AuthData> authData = authDataRepository.findByEmail(username);

        if (authData.isEmpty()) {
            throw new UserNotFoundException("Пользователь c почтой (Email: " + username + ") не найден.");
        }

        return authData.get().getUser();
    }
}
