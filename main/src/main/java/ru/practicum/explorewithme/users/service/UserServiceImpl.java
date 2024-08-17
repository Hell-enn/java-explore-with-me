package ru.practicum.explorewithme.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.users.mapper.UserMapper;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        List<User> userWithSameEmail = userRepository.findByEmail(newUserRequest.getEmail());
        if (!userWithSameEmail.isEmpty())
            throw new ConflictException(
                    "Пользователь с электронной почтой " + newUserRequest.getEmail() + " уже существует!");

        UserDto userDto = userMapper.userToUserDto(userRepository.save(userMapper.newUserRequestToUser(newUserRequest)));
        log.debug("Публикация информации о новом пользователе прошла успешно!\n{}", newUserRequest);

        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не существует!"));

        userRepository.deleteById(userId);
        log.debug("Удаление информации о пользователе с id ={} прошло успешно!", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(Integer from, Integer size, List<Long> ids) {
        int amountOfUsers = userRepository.findAmount();
        int pageNum = amountOfUsers > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<UserDto> userDtos = userMapper.userToUserDtoList(
                ids == null || ids.isEmpty() ? userRepository.findAllUsers(page) : userRepository.findAllByIds(ids, page));
        log.debug("Получение списка пользователей с id = {} с позиции {} в количестве {} прошло успешно!", ids, from, size);

        return userDtos;
    }
}
