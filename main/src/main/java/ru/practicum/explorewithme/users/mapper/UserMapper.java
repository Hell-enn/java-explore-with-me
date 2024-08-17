package ru.practicum.explorewithme.users.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.dto.UserShortDto;
import ru.practicum.explorewithme.users.model.User;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Component
public class UserMapper {

    public User newUserRequestToUser(NewUserRequest newUserRequest) {
        return new User(null, newUserRequest.getEmail(), newUserRequest.getName());
    }

    public UserDto userToUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public UserShortDto userToUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public User updateUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getEmail(), userDto.getName());
    }

    public List<UserDto> userToUserDtoList(Iterable<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        users.forEach(user -> userDtos.add(new UserDto(user.getId(), user.getEmail(), user.getName())));
        return userDtos;
    }
}
