package ru.practicum.explorewithme.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.*;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.repository.CategoryRepository;
import ru.practicum.explorewithme.compilations.dto.RequestAmountDto;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.dto.enums.State;
import ru.practicum.explorewithme.events.dto.enums.StateAction;
import ru.practicum.explorewithme.events.model.Location;
import ru.practicum.explorewithme.events.repository.LocationRepository;
import ru.practicum.explorewithme.requests.dto.enums.Status;
import ru.practicum.explorewithme.events.dto.*;
import ru.practicum.explorewithme.events.mapper.EventMapper;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.ForbiddenException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.requests.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.requests.model.ParticipationRequest;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {

        findUser(userId);

        int amountOfEvents = eventRepository.findUserEventsAmount(userId);
        int pageNum = amountOfEvents > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<Event> events = eventRepository.findByUserIdOrderByEventDate(userId, page);
        List<Long> eventIds = new ArrayList<>();
        events.forEach(event -> eventIds.add(event.getId()));
        Map<Long, Long> requestAmounts = findRequestAmountList(eventIds);

        List<EndpointStatisticsDto> endpointStatisticsDtos = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                events
        );
        List<EventShortDto> eventShortDtos = eventMapper
                .eventToEventShortDtoList(events, requestAmounts, endpointStatisticsDtos);
        log.debug("Возвращение списка событий пользователя с id = {} с позиции {} в количестве {}\n{}",
                userId, from, size, eventShortDtos);

        return eventShortDtos;

    }


    @Override
    @Transactional
    public EventFullDto postEvent(NewEventDto newEventDto, Long userId) {

        if (LocalDateTime.now().isAfter(newEventDto.getEventDate().minusHours(2)))
            throw new ForbiddenException("Время начала мероприятия не может быть раньше, чем за 2 часа до настоящего момента!");

        Category category = findCategory(newEventDto.getCategory());
        Location location = findLocation(newEventDto.getLocation());
        User user = findUser(userId);

        Event newEvent = eventRepository.save(eventMapper.newEventDtoToEvent(newEventDto, category, location, user));

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                List.of(newEvent)
        );

        Long requestAmount = Long.valueOf(findRequestAmount(newEvent.getId()));
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(newEvent, endpointStatisticsDto, requestAmount);

        log.debug("Публикация события пользователем с id = {} прошла успешно!\n{}", userId, eventFullDto);

        return eventFullDto;
    }


    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {

        findUser(userId);

        Event event = eventRepository.findByEventIdAndUserId(eventId, userId);
        if (event == null)
            throw new NotFoundException("Событие с id = " + eventId + " пользователя с id = " + userId + " не найдено!");

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                List.of(event)
        );
        Long requestAmount = Long.valueOf(findRequestAmount(event.getId()));

        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event, endpointStatisticsDto, requestAmount);
        log.debug("Получение пользователем с id = {} события с id = {} прошло успешно!\n{}", userId, eventId, eventFullDto);

        return eventFullDto;
    }


    @Override
    @Transactional
    public EventFullDto patchEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {

        findUser(userId);

        Event event = eventRepository.findByEventIdAndUserId(eventId, userId);
        if (event == null)
            throw new NotFoundException("Событие с id = " + eventId + " пользователя с id = " + userId + " не найдено!");

        State state = event.getState();
        if (!(state.equals(State.CANCELED) || state.equals(State.PENDING)))
            throw new ConflictException("Статус события при изменении может быть только CANCELED или PENDING!");

        LocalDateTime eventDate = updateEventUserRequest.getEventDate();
        if (eventDate != null)
            if (LocalDateTime.now().isAfter(eventDate.minusHours(2)))
                throw new ConflictException("Нельзя изменить время начала мероприятия на время меньшее, чем текущий момент!");


        Long catId = updateEventUserRequest.getCategory();
        Category category = catId == null ? null : findCategory(catId);

        LocationDto locationDto = updateEventUserRequest.getLocation();
        Location location = locationDto == null ? null : findLocation(locationDto);

        Event updatedEvent = eventRepository
                .save(eventMapper.updateEventUser(updateEventUserRequest, event, category, location));

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                List.of(event)
        );
        Long requestAmount = Long.valueOf(findRequestAmount(event.getId()));

        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(updatedEvent, endpointStatisticsDto, requestAmount);
        log.debug("Обновление пользователем с id = {} события с id = {} прошло успешно!\n{}", userId, eventId, eventFullDto);

        return eventFullDto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {

        findUser(userId);

        Event event = eventRepository.findByEventIdAndUserId(eventId, userId);
        if (event == null)
            throw new NotFoundException("Событие с id = " + eventId + " пользователя с id = " + userId + " не найдено!");

        List<ParticipationRequestDto> participationRequestDtos = participationRequestMapper
                                            .participationRequestToParticipationRequestDtoList(
                                                    participationRequestRepository.findByEventId(eventId));
        log.debug("Получение пользователем с id = {} запросов на участие в событии с id = {} прошло успешно!\n{}",
                userId, eventId, participationRequestDtos);

        return participationRequestDtos;

    }


    @Override
    @Transactional
    public EventRequestStatusUpdateResult patchUserEventRequest(Long userId,
                                                                Long eventId,
                                                                EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        Event event = eventRepository.findByEventIdAndUserId(eventId, userId);
        if (event == null)
            throw new NotFoundException("Событие с id = " + eventId + " пользователя с id = " + userId + " не найдено!");

        int participationLimit = event.getParticipantLimit();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (participationLimit > 0 || event.getRequestModeration()) {

            int confirmedRequestsAmount = participationRequestRepository.findByEventIdAndStatusAmount(
                    eventId, Status.CONFIRMED.toString());
            if (confirmedRequestsAmount >= participationLimit)
                throw new ConflictException("Количество заявок в запросе на одобрение превышает лимит заявок!");

            List<Long> participationRequestIds = eventRequestStatusUpdateRequest.getRequestIds();
            List<ParticipationRequest> existedParticipationRequests =
                    participationRequestRepository.findByEventIdAndRequestIdOrderByRequestId(eventId, participationRequestIds);
            Status status = Status.valueOf(eventRequestStatusUpdateRequest.getStatus());

            int idsToConfirm = participationLimit - confirmedRequestsAmount;

            if (status.equals(Status.CONFIRMED)) {
                existedParticipationRequests
                        .stream()
                        .limit(idsToConfirm)
                        .forEachOrdered(existedParticipationRequest -> {
                            if (!existedParticipationRequest.getStatus().equals(Status.PENDING))
                                throw new ConflictException("Статус заявки с id = " + existedParticipationRequest.getId() +
                                        " нельзя изменить, так как на данный момент её статус - " +
                                        existedParticipationRequest.getStatus());

                            existedParticipationRequest.setStatus(Status.CONFIRMED);
                            confirmedRequests
                                    .add(participationRequestMapper
                                            .participationRequestToParticipationRequestDto(existedParticipationRequest));
                        });

                existedParticipationRequests
                        .stream()
                        .skip(idsToConfirm)
                        .forEachOrdered(existedParticipationRequest -> {
                            if (!existedParticipationRequest.getStatus().equals(Status.PENDING))
                                throw new ConflictException("Статус заявки с id = " + existedParticipationRequest.getId() +
                                        " нельзя изменить, так как на данный момент её статус - " +
                                        existedParticipationRequest.getStatus());

                            existedParticipationRequest.setStatus(Status.REJECTED);
                            rejectedRequests
                                    .add(participationRequestMapper
                                            .participationRequestToParticipationRequestDto(existedParticipationRequest));
                        });
            } else {
                existedParticipationRequests
                        .forEach(existedParticipationRequest -> {
                            if (!existedParticipationRequest.getStatus().equals(Status.PENDING))
                                throw new ConflictException("Статус заявки с id = " + existedParticipationRequest.getId() +
                                        " нельзя изменить, так как на данный момент её статус - " +
                                        existedParticipationRequest.getStatus());

                            existedParticipationRequest.setStatus(status);
                            rejectedRequests
                                    .add(participationRequestMapper
                                            .participationRequestToParticipationRequestDto(existedParticipationRequest));
                        });
            }

            participationRequestRepository.saveAll(existedParticipationRequests);
        }

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult(
                confirmedRequests, rejectedRequests);
        log.debug("Обновление пользователем с id = {} запросов на участие в событии с id = {} прошло успешно!\n{}",
                userId, eventId, eventRequestStatusUpdateResult);

        return eventRequestStatusUpdateResult;
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                  List<String> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Integer from,
                                  Integer size) {

        if (users == null || users.isEmpty() || (users.size() == 1 && users.get(0) == 0))
            users = userRepository.findUserIds();

        if (states == null || states.isEmpty()) {
            List<String> finalStates = new ArrayList<>();
            Arrays.stream(State.values()).forEach(state -> finalStates.add(state.toString()));
            states = finalStates;
        }

        if (categories == null || categories.isEmpty() || (categories.size() == 1 && categories.get(0) == 0))
            categories = categoryRepository.findCategoryIds();

        if (rangeStart == null)
            rangeStart = LocalDateTime.now().minusYears(100);

        if (rangeEnd == null)
            rangeEnd = LocalDateTime.now().plusYears(100);

        int amountOfEvents = eventRepository.findAdminEventsAmount(users, states, categories, rangeStart, rangeEnd);
        int pageNum = amountOfEvents > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<Event> events = eventRepository.findAdminEvents(users, states, categories, rangeStart, rangeEnd, page);
        List<Long> eventIds = new ArrayList<>();
        events.forEach(event -> eventIds.add(event.getId()));

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                events
        );
        Map<Long, Long> requestAmount = findRequestAmountList(eventIds);

        List<EventFullDto> eventFullDtos = eventMapper.eventToEventFullDtoList(events, endpointStatisticsDto, requestAmount);
        log.debug("Возвращение списка событий пользователей с id = {} в состояниях {} категорий {} с момента {} до момента " +
                "{} с позиции {} в количестве {}", users, states, categories, rangeStart, rangeEnd, from, size);

        return eventFullDtos;
    }


    @Override
    @Transactional
    public EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено!"));

        LocalDateTime eventDate = updateEventAdminRequest.getEventDate();
        if (eventDate != null)
            if (LocalDateTime.now().isAfter(eventDate.minusHours(1)))
                throw new ConflictException("Нельзя изменить время начала мероприятия на время меньшее, чем текущий момент!");

        State state = event.getState();
        String stateAction = updateEventAdminRequest.getStateAction();
        if (stateAction != null) {
            if (stateAction.toUpperCase().equals(StateAction.PUBLISH_EVENT.toString()) && !state.equals(State.PENDING))
                throw new ConflictException("Статус события при публикации может быть только PENDING!");

            if (stateAction.toUpperCase().equals(StateAction.REJECT_EVENT.toString()) && state.equals(State.PUBLISHED))
                throw new ConflictException("Статус события при его отклонении не может быть PUBLISHED!");
        }

        Long catId = updateEventAdminRequest.getCategory();
        Category category = catId == null ? null : findCategory(catId);

        LocationDto locationDto = updateEventAdminRequest.getLocation();
        Location location = locationDto == null ? null : findLocation(locationDto);

        Event updatedEvent = eventRepository
                .save(eventMapper
                        .updateEventAdmin(updateEventAdminRequest, event, category, location));

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                List.of(event)
        );
        Long requestAmount = Long.valueOf(findRequestAmount(event.getId()));

        EventFullDto eventFullDto = eventMapper
                .eventToEventFullDto(updatedEvent, endpointStatisticsDto, requestAmount);
        log.debug("Обновление события с id = {} прошло успешно!\n{}", eventId, eventFullDto);

        return eventFullDto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getPublicEvents(String text,
                                              List<Long> categories,
                                              Boolean paid,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              Boolean onlyAvailable,
                                              String sort,
                                              Integer from,
                                              Integer size,
                                              HttpServletRequest request) {

        if (text == null)
            text = "";

        if (categories == null || categories.isEmpty())
            categories = categoryRepository.findCategoryIds();

        List<Boolean> paidList;
        if (paid == null)
            paidList = List.of(true, false);
        else
            paidList = List.of(paid);

        if (rangeStart == null)
            rangeStart = LocalDateTime.now().minusYears(100);

        if (rangeEnd == null)
            rangeEnd = LocalDateTime.now().plusYears(100);

        int amountOfEvents = eventRepository
                .findPublicEventsAmount(text, text, categories, paidList, rangeStart, rangeEnd);
        int pageNum = amountOfEvents > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<Event> events;
        switch (sort.toUpperCase()) {
            case "EVENT_DATE": {
                events = eventRepository.findPublicEventsWithSortingByEventDate(text,
                                                                                text,
                                                                                categories,
                                                                                paidList,
                                                                                rangeStart,
                                                                                rangeEnd,
                                                                                page);
                break;
            }
            case "VIEWS": {
                events = eventRepository.findPublicEvents(text, text, categories, paidList, rangeStart, rangeEnd, page);
                break;
            }
            default: {
                events = eventRepository.findPublicEventsWithSortingByEventId(text,
                                                                              text,
                                                                              categories,
                                                                              paidList,
                                                                              rangeStart,
                                                                              rangeEnd,
                                                                              page);
            }
        }

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                events
        );
        List<Long> eventIds = new ArrayList<>();
        events.forEach(event -> eventIds.add(event.getId()));
        Map<Long, Long> requestAmounts = findRequestAmountList(eventIds);

        List<EventFullDto> eventFullDtos = eventMapper.eventToEventFullDtoList(events, endpointStatisticsDto, requestAmounts);
        if (onlyAvailable)
            eventFullDtos = eventFullDtos
                                    .stream()
                                    .filter(eventFullDto ->
                                            eventFullDto.getParticipantLimit() > eventFullDto.getConfirmedRequests())
                                    .collect(Collectors.toList());

        if (sort.equals("VIEWS"))
            eventFullDtos.sort(Comparator.comparing(EventFullDto::getViews));

        statsClient.postHit(request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        log.debug("Возвращение списка событий по подстроке \"{}\" категорий {}, где флаг оплаты = {} и наличие " +
                "свободных мест = {} с момента {} до момента {} с позиции {} в количестве {}:\n{}",
                text, categories, paid, onlyAvailable, rangeStart, rangeEnd, from, size, eventFullDtos);

        return eventFullDtos;
    }


    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository
                .findPublicEvent(eventId, State.PUBLISHED.toString())
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с id = " + eventId + " не найдено!"));

        statsClient.postHit(request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        List<EndpointStatisticsDto> endpointStatisticsDto = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                List.of(event)
        );
        Long requestAmount = Long.valueOf(findRequestAmount(event.getId()));

        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event, endpointStatisticsDto, requestAmount);

        log.debug("Получение события с id = {} прошло успешно!\n{}", eventId, eventFullDto);

        return eventFullDto;
    }


    private Category findCategory(Long catId) {
        return categoryRepository
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найдена!"));
    }


    private Location findLocation(LocationDto locationDto) {
        List<Location> locationList = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        return !locationList.isEmpty() ?
                        locationList.get(0) :
                        locationRepository.save(new Location(null, locationDto.getLat(), locationDto.getLon()));
    }


    private User findUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));
    }


    private Integer findRequestAmount(Long eventId) {
        return participationRequestRepository.findRequestAmount(eventId, Status.CONFIRMED.toString());
    }


    private Map<Long, Long> findRequestAmountList(List<Long> eventIds) {
        Map<Long, Long> requestAmounts = new HashMap<>();
        List<RequestAmountDto> requestAmountDtos = participationRequestRepository.findRequestAmount(eventIds);
        requestAmountDtos.forEach(requestAmountDto -> requestAmounts.put(
                requestAmountDto.getEventId(),
                requestAmountDto.getRequestAmount()));
        return requestAmounts;
    }


    @SneakyThrows
    private List<EndpointStatisticsDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<Event> events) {
        List<String> uris = new ArrayList<>();
        events.forEach(event -> uris.add("/events/" + event.getId()));

        ResponseEntity<Object> objResults = statsClient
                .getPeriodUrisUniqueStats(start, end, uris, true);
        return objectMapper.readValue(objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});
    }
}