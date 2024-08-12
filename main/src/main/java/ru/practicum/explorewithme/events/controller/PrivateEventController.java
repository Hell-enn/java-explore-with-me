package ru.practicum.explorewithme.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.service.EventService;
import ru.practicum.explorewithme.requests.dto.EventRequestStatusUpdateRequest;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventServiceImpl;

    @GetMapping
    public ResponseEntity<Object> getUserEvents(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0")Integer from,
                                                @RequestParam(defaultValue = "10")Integer size) {
        log.debug("Принят запрос на получение списка событий пользователя с id = {} с позиции {} в количестве {}",
                userId, from, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getUserEvents(userId, from, size));
    }


    @PostMapping
    public ResponseEntity<Object> postEvent(@Valid @RequestBody NewEventDto newEventDto,
                                            @PathVariable Long userId) {
        log.debug("Принят запрос на публикацию пользователем с id = {} события:" +
                        "\n\tаннотация: {}" +
                        "\n\tкатегория: {}" +
                        "\n\tописание: {}" +
                        "\n\tдата события: {}" +
                        "\n\tлокация: {}" +
                        "\n\tфлаг оплаты: {}" +
                        "\n\tмаксимальное количество участников: {}" +
                        "\n\tфлаг премодерации: {}" +
                        "\n\tзаголовок: {}",
                userId,
                newEventDto.getAnnotation(),
                newEventDto.getCategory(),
                newEventDto.getDescription(),
                newEventDto.getEventDate(),
                newEventDto.getLocation(),
                newEventDto.getPaid(),
                newEventDto.getParticipantLimit(),
                newEventDto.getRequestModeration(),
                newEventDto.getTitle());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventServiceImpl.postEvent(newEventDto, userId));
    }


    @GetMapping("/{eventId}")
    public ResponseEntity<Object> getEvent(@PathVariable Long userId,
                                           @PathVariable Long eventId) {
        log.debug("Принят запрос на получение пользователем с id = {} события с id = {}", userId, eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getEvent(userId, eventId));
    }


    @PatchMapping("/{eventId}")
    public ResponseEntity<Object> patchEvent(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.debug("Принят запрос на обновление пользователем с id = {} события с id = {} объектом:" +
                        "\n\tаннотация: {}" +
                        "\n\tкатегория: {}" +
                        "\n\tописание: {}" +
                        "\n\tдата события: {}" +
                        "\n\tлокация: {}" +
                        "\n\tфлаг оплаты: {}" +
                        "\n\tмаксимальное число участников: {}" +
                        "\n\tфлаг необходимости модерации: {}" +
                        "\n\tзаголовок: {}" +
                        "\n\tдействие: {}",
                userId, eventId,
                updateEventUserRequest.getAnnotation(),
                updateEventUserRequest.getCategory(),
                updateEventUserRequest.getDescription(),
                updateEventUserRequest.getEventDate(),
                updateEventUserRequest.getLocation(),
                updateEventUserRequest.getPaid(),
                updateEventUserRequest.getParticipantLimit(),
                updateEventUserRequest.getRequestModeration(),
                updateEventUserRequest.getTitle(),
                updateEventUserRequest.getStateAction());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.patchEventByUser(userId, eventId, updateEventUserRequest));
    }


    @GetMapping("/{eventId}/requests")
    public ResponseEntity<Object> getUserEventRequests(@PathVariable Long userId,
                                                       @PathVariable Long eventId) {
        log.debug("Принят запрос на получение пользователем с id = {} списка запросов на участие в событии с id = {}",
                userId, eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getUserEventRequests(userId, eventId));
    }


    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<Object> patchUserEventRequest(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.debug("Принят запрос на обновление пользователем с id = {} статусов запросов на участие в событии с id = {}." +
                        "\nсписок запросов = {}" +
                        "\nстатус обновления = {}",
                userId, eventId,
                eventRequestStatusUpdateRequest.getRequestIds(),
                eventRequestStatusUpdateRequest.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.patchUserEventRequest(userId, eventId, eventRequestStatusUpdateRequest));
    }
}