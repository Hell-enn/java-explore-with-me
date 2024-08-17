package ru.practicum.explorewithme.subscription.service;

import ru.practicum.explorewithme.events.dto.EventFullDto;
import ru.practicum.explorewithme.subscription.dto.SubscriptionDto;
import ru.practicum.explorewithme.subscription.model.Subscription;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDto addSubscription(Long followerId, Long followedId);

    void deleteSubscription(Long followerId, Long followedId);

    List<EventFullDto> getFollowedUsersEvents(Long followerId, Integer from, Integer size);
}
