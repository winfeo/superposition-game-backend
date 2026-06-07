package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.dto.fromApp.NewUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.fromApp.UpdateUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;
import io.github.winfeo.superpositiongame.backend.entity.db.AuthData;
import io.github.winfeo.superpositiongame.backend.entity.db.League;
import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.exception.*;
import io.github.winfeo.superpositiongame.backend.repository.*;
import io.github.winfeo.superpositiongame.backend.service.UserService;
import io.github.winfeo.superpositiongame.backend.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthDataRepository authDataRepository;
    private final LeagueRepository leagueRepository;
    private final GameRepository gameRepository;
    private final UserAchievementRepository achievementRepository;
    private final GamePlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::convertToDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .map(UserMapper::convertToDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь c ником " + nickname + " не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        Optional<AuthData> authData = authDataRepository.findByEmail(email);

        if (authData.isEmpty()) {
            throw new UserNotFoundException("Пользователь c почтой " + email + " не найден");
        }

        User user = authData.get().getUser();
        return UserMapper.convertToDto(user);
    }

    @Override
    @Transactional
    public UserDTO createUser(NewUserDTO dto) {
        boolean isEmailExists = authDataRepository.existsByEmail(dto.getEmail());
        if (isEmailExists) {
            throw new AuthDataEmailAlreadyTakenException("Почта " + dto.getEmail() + " уже занята");
        }

        League startLeague = leagueRepository.findByMinRating(0)
                .orElseThrow(() -> new LeagueNotFoundException("Лига с мин. рейтингом " + 0 + " не найдена"));

        User user = UserMapper.convertToDomain(dto, startLeague); //TODO добавить encoder
        try {
            return UserMapper.convertToDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserSaveFailedException("Не удалось сохранить пользователя");
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(UpdateUserDTO dto) {
        User user = userRepository.findById(dto.getId()).orElseThrow(() ->
                new UserNotFoundException("Пользователь c id " + dto.getId() + " не найден"));

        AuthData authData = user.getAuthData();
        String newEmail = dto.getEmail();
        if (!authData.getEmail().equals(newEmail) && !newEmail.isEmpty()) {
            if (authDataRepository.existsByEmail(newEmail)) {
                throw new AuthDataEmailAlreadyTakenException("Почта " + newEmail + " уже занята");
            }

            authData.setEmail(newEmail);
        }

        String newNickname = dto.getNickname();
        if (!user.getNickname().equals(newNickname) && !newNickname.isEmpty()) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new NicknameAlreadyTakenException("Никнейм " + newNickname + " уже занят");
            }

            if (newNickname.isBlank()) {
                throw new NicknameIsEmptyException("Никнейм не должен быть пустым");
            }

            user.setNickname(newNickname);
        }

        return UserMapper.convertToDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Пользователь c id " + id + " не найден");
        }

        gameRepository.resetWinnerId(id);
        achievementRepository.deleteByUserId(id);
        playerRepository.deleteByUserId(id);

        userRepository.deleteById(id);
    }
}
