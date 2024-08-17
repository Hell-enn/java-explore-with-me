package ru.practicum.explorewithme.subscription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.subscription.service.SubscriptionService;

@RestController
@RequestMapping("/subscriptions/follower/{followerId}")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SubscriptionController {

    private final SubscriptionService subscriptionServiceImpl;

    @PostMapping("/followed/{followedId}")
    public ResponseEntity<Object> postSubscription(@PathVariable Long followerId,
                                                   @PathVariable Long followedId) {
        log.debug("Принят запрос на публикацию подписки пользователя с id = {} на пользователя с id = {}",
                followerId, followedId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscriptionServiceImpl.addSubscription(followerId, followedId));
    }


    @DeleteMapping("/followed/{followedId}")
    public ResponseEntity<Object> deleteSubscription(@PathVariable Long followerId,
                                                     @PathVariable Long followedId) {
        log.debug("Принят запрос на удаление подписки пользователя с id = {} на пользователя с id = {}",
                followerId, followedId);
        subscriptionServiceImpl.deleteSubscription(followerId, followedId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


    @GetMapping
    public ResponseEntity<Object> getFollowedUsersEvents(@PathVariable Long followerId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        log.debug("Принят запрос на получение списка событий пользователей, на которых подписан пользователь с id = {} " +
                "с позиции {} в количестве {}", followerId, from, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subscriptionServiceImpl.getFollowedUsersEvents(followerId, from, size));
    }
}
