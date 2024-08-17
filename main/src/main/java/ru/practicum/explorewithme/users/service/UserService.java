package ru.practicum.explorewithme.users.service;

import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    List<UserDto> getUsers(Integer from, Integer size, List<Long> ids);
}