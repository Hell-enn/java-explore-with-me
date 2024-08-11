package ru.practicum.explorewithme.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.requests.service.ParticipationRequestService;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@PathVariable Long userId) {
        log.debug("Принят запрос на получение пользователем с id = {} списка своих запросов на участие в событиях", userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(participationRequestService.getUserRequests(userId));
    }


    @PostMapping
    public ResponseEntity<Object> postUserRequest(@PathVariable Long userId,
                                                  @RequestParam Long eventId) {
        log.debug("Принят запрос на публикацию пользователем с id = {} запроса на участие в событии с id = {}", userId, eventId);
        return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(participationRequestService.postUserRequest(userId, eventId));
    }


    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<Object> cancelUserParticipationRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.debug("Принят запрос на отмену пользователем с id = {} своего запроса на участие с id = {}", userId, requestId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(participationRequestService.cancelUserParticipationRequest(userId, requestId));
    }
}
