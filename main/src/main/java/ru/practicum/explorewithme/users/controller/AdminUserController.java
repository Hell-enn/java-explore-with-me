package ru.practicum.explorewithme.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.exceptions.BadRequestException;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminUserController {

    private final UserService userServiceImpl;

    @PostMapping
    public ResponseEntity<Object> postUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.debug("""
                        Принят запрос на публикация нового пользователя:
                        \temail: {},
                        \tname: {}""",
                newUserRequest.getEmail(), newUserRequest.getName());
        if (newUserRequest.getName().trim().isEmpty())
            throw new BadRequestException("Вы передали пустое имя пользователя!");
        return ResponseEntity.status(HttpStatus.CREATED).body(userServiceImpl.addUser(newUserRequest));
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@Valid @PathVariable Long userId) {
        log.debug("Принят запрос на удаление пользователя с id = {}", userId);
        userServiceImpl.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping
    public ResponseEntity<Object> getUsers(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) List<Long> ids) {
        log.debug("Принят запрос на получение списка пользователей с id = {} с позиции {} в количестве {}", ids, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(userServiceImpl.getUsers(from, size, ids));
    }
}
