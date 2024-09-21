package ru.practicum.explorewithme.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.dto.EventFullDto;
import ru.practicum.explorewithme.events.mapper.EventMapper;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exceptions.BadRequestException;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.subscription.dto.SubscriptionDto;
import ru.practicum.explorewithme.subscription.mapper.SubscriptionMapper;
import ru.practicum.explorewithme.subscription.model.Subscription;
import ru.practicum.explorewithme.subscription.repository.SubscriptionRepository;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;
import ru.practicum.explorewithme.utils.UtilService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UtilService utilService;

    @Override
    @Transactional
    public SubscriptionDto addSubscription(Long followerId, Long followedId) {
        User follower = userRepository
                .findById(followerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followerId + " не найден!"));

        User followed = userRepository
                .findById(followedId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followedId + " не найден!"));

        if (followedId.equals(followerId))
            throw new BadRequestException("Нельзя подписаться на собственные события!");

        Optional<Subscription> subscription = subscriptionRepository.findByFollowerAndFollowed(followerId, followedId);
        if (subscription.isPresent())
            throw new ConflictException("Подписка пользователя с id = " + followerId + " на пользователя с id = " + followedId
            + " уже существует!");

        Subscription savedSubscription = subscriptionRepository.save(new Subscription(null, follower, followed));

        SubscriptionDto subscriptionDto = subscriptionMapper.subscriptionToSubscriptionDto(savedSubscription);
        log.debug("Публикация информации о новой подписке пользователя с id = {} на пользователя с id = {} прошла успешно!",
                followerId, followedId);

        return subscriptionDto;
    }

    @Override
    @Transactional
    public void deleteSubscription(Long followerId, Long followedId) {
        userRepository
                .findById(followerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followerId + " не найден!"));

        userRepository
                .findById(followedId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followedId + " не найден!"));

        Subscription subscription = subscriptionRepository
                .findByFollowerAndFollowed(followerId, followedId)
                .orElseThrow(() -> new NotFoundException(
                        "Подписка пользователя с id = " + followerId + " на пользователя с id = " + followedId
                        + " не существует!"));

        subscriptionRepository.deleteById(subscription.getId());
        log.debug("Информации информации о подписке пользователя с id = {} на пользователя с id = {} прошла успешно!",
                followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getFollowedUsersEvents(Long followerId, Integer from, Integer size) {
        userRepository
                .findById(followerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followerId + " не найден!"));

        int amountOfEvents = eventRepository.findFollowedUsersEventsAmount(followerId);
        int pageNum = amountOfEvents > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<Event> events = eventRepository.findFollowedUsersEvents(followerId, page);

        List<EndpointStatisticsDto> endpointStatisticsDto = utilService.getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                events
        );
        List<Long> eventIds = new ArrayList<>();
        events.forEach(event -> eventIds.add(event.getId()));
        Map<Long, Long> requestAmounts = utilService.findRequestAmountList(eventIds);

        return eventMapper.eventToEventFullDtoList(events, endpointStatisticsDto, requestAmounts);
    }
}