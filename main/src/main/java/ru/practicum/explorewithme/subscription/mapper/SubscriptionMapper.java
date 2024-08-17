package ru.practicum.explorewithme.subscription.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.subscription.dto.SubscriptionDto;
import ru.practicum.explorewithme.subscription.model.Subscription;
import ru.practicum.explorewithme.users.mapper.UserMapper;

@AllArgsConstructor
@Component
public class SubscriptionMapper {

    private final UserMapper userMapper;

    public SubscriptionDto subscriptionToSubscriptionDto(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getId(),
                userMapper.userToUserDto(subscription.getFollower()),
                userMapper.userToUserDto(subscription.getFollowed()));
    }
}
