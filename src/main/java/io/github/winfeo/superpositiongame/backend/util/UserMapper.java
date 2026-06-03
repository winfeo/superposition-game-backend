package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.dto.fromApp.NewUserDTO;
import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;
import io.github.winfeo.superpositiongame.backend.entity.db.AuthData;
import io.github.winfeo.superpositiongame.backend.entity.db.League;
import io.github.winfeo.superpositiongame.backend.entity.db.User;
import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class UserMapper {
    private final Random random = new Random();

    public UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setLeague(user.getLeague().getTitle());
        userDTO.setNickname(user.getNickname());
        userDTO.setRatingPoints(user.getRatingPoints());
        userDTO.setWinsAmount(user.getWinsAmount());
        userDTO.setGamesPlayed(user.getGamesPlayed());
        userDTO.setCreatedAt(user.getCreatedAt());

        return userDTO;
    }

    public User convertToDomain(NewUserDTO dto, League league) {
        int randomId = random.nextInt(10_000, 100_000);

        AuthData authData = new AuthData();
        authData.setEmail(dto.getEmail());
        authData.setPasswordHash(dto.getPassword()); //TODO доделать потом через encoder

        User user = new User();
        user.setAuthData(authData);
        user.setLeague(league);
        user.setNickname("Player-" + randomId); //TODO ставить id игрока (первые 5 цифр)
        user.setRatingPoints(0);
        user.setWinsAmount(0);
        user.setGamesPlayed(0);

        return user;
    }
}
