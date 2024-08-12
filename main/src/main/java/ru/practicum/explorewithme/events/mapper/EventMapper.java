package ru.practicum.explorewithme.events.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.*;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.repository.CategoryRepository;
import ru.practicum.explorewithme.events.dto.enums.State;
import ru.practicum.explorewithme.events.dto.enums.StateAction;
import ru.practicum.explorewithme.requests.dto.enums.Status;
import ru.practicum.explorewithme.events.dto.*;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.model.Location;
import ru.practicum.explorewithme.events.repository.LocationRepository;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.users.dto.UserShortDto;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class EventMapper {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public Event newEventDtoToEvent(NewEventDto newEventDto, Long userId) {

        Long catId = newEventDto.getCategory();
        Category category = categoryRepository
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найдена!"));

        LocationDto locationDto = newEventDto.getLocation();
        List<Location> locationList = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        Location location =
                !locationList.isEmpty() ?
                        locationList.get(0) :
                        locationRepository.save(new Location(null, locationDto.getLat(), locationDto.getLon()));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        Boolean paid = newEventDto.getPaid();
        Integer participantLimit = newEventDto.getParticipantLimit();
        Boolean requestModeration = newEventDto.getRequestModeration();
        return new Event(
                null,
                newEventDto.getAnnotation(),
                category,
                newEventDto.getDescription(),
                newEventDto.getEventDate(),
                LocalDateTime.now(),
                null,
                location,
                paid != null && paid,
                participantLimit == null ? 0 : participantLimit,
                requestModeration == null || requestModeration,
                user,
                newEventDto.getTitle(),
                State.PENDING
        );
    }

    public Event eventFullDtoToEvent(EventFullDto eventFullDto, Long userId) {

        CategoryDto categoryDto = eventFullDto.getCategory();
        Category category = new Category(categoryDto.getId(), categoryDto.getName());

        LocationDto locationDto = eventFullDto.getLocation();
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        return new Event(
                eventFullDto.getId(),
                eventFullDto.getAnnotation(),
                category,
                eventFullDto.getDescription(),
                eventFullDto.getEventDate(),
                eventFullDto.getCreatedOn(),
                eventFullDto.getPublishedOn(),
                new Location(null, locationDto.getLat(), locationDto.getLon()),
                eventFullDto.getPaid(),
                eventFullDto.getParticipantLimit(),
                eventFullDto.getRequestModeration(),
                user,
                eventFullDto.getTitle(),
                State.valueOf(eventFullDto.getState().toUpperCase())
        );
    }

    private EventFullDto eventToEventFullDto(Event event, Long views) {

        Category category = event.getCategory();
        CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName());

        Long eventId = event.getId();

        User user = event.getUser();
        UserShortDto userShortDto = new UserShortDto(user.getId(), user.getName());

        Location location = event.getLocation();
        LocationDto locationDto = new LocationDto(location.getLat(), location.getLon());

        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                categoryDto,
                participationRequestRepository.findRequestAmount(eventId, Status.CONFIRMED.toString()),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                userShortDto,
                locationDto,
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState().name(),
                event.getTitle(),
                views
        );
    }


    @SneakyThrows
    public EventFullDto eventToEventFullDto(Event event) {

        LocalDateTime start = event.getPublishedOn() == null ? LocalDateTime.now() : event.getPublishedOn();
        LocalDateTime end = event.getEventDate() == null ? LocalDateTime.now().plusYears(100) : event.getEventDate();

        ResponseEntity<Object> objResults = statsClient.getPeriodUrisStats(start, end, List.of("/events/" + event.getId()));
        List<EndpointStatisticsDto> endpointStatisticsDtos = objectMapper.readValue(
                objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});

        EndpointStatisticsDto endpointStatisticsDto = endpointStatisticsDtos.get(0);
        Long views = endpointStatisticsDto == null ? 0 : endpointStatisticsDto.getHits();
        return eventToEventFullDto(event, views);
    }


    public EventShortDto eventToEventShortDto(Event event, Long views) {

        Category category = event.getCategory();
        CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName());

        Long eventId = event.getId();

        User user = event.getUser();
        UserShortDto userShortDto = new UserShortDto(user.getId(), user.getName());

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryDto,
                participationRequestRepository.findRequestAmount(eventId, Status.CONFIRMED.toString()),
                event.getEventDate(),
                userShortDto,
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public Event updateEventAdmin(UpdateEventAdminRequest updateEventAdminRequest, Event event) {

        Event updatedEvent = updateEventCommon(updateEventAdminRequest, event);

        String stateAction = updateEventAdminRequest.getStateAction();
        if (stateAction != null) {
            if (stateAction.equals(StateAction.PUBLISH_EVENT.toString())) {
                updatedEvent.setState(State.PUBLISHED);
                updatedEvent.setPublishedOn(LocalDateTime.now());
            } else if (stateAction.equals(StateAction.REJECT_EVENT.toString()))
                updatedEvent.setState(State.CANCELED);
        }

        return updatedEvent;
    }

    public Event updateEventUser(UpdateEventUserRequest updateEventUserRequest, Event event) {

        Event updatedEvent = updateEventCommon(updateEventUserRequest, event);

        String stateAction = updateEventUserRequest.getStateAction();
        if (stateAction != null) {
            if (stateAction.equals(StateAction.CANCEL_REVIEW.toString()))
                updatedEvent.setState(State.CANCELED);
            else if (stateAction.equals(StateAction.SEND_TO_REVIEW.toString()))
                updatedEvent.setState(State.PENDING);
        }

        return updatedEvent;
    }


    private Event updateEventCommon(UpdateEventCommonRequest updateEventCommonRequest, Event event) {
        String annotation = updateEventCommonRequest.getAnnotation();
        if (annotation != null && annotation.length() >= 20 && annotation.length() <= 2000)
            event.setAnnotation(annotation);

        Long catId = updateEventCommonRequest.getCategory();
        if (catId != null) {
            Category category = categoryRepository
                    .findById(catId)
                    .orElseThrow(() -> new NotFoundException("Категория с идентификатором " + catId + " не найдена!"));
            event.setCategory(category);
        }

        String description = updateEventCommonRequest.getDescription();
        if (description != null && description.length() >= 20 && description.length() <= 7000)
            event.setDescription(description);

        LocalDateTime eventDate = updateEventCommonRequest.getEventDate();
        if (eventDate != null)
            event.setEventDate(eventDate);

        LocationDto locationDto = updateEventCommonRequest.getLocation();
        if (locationDto != null) {
            Double lat = locationDto.getLat();
            Double lon = locationDto.getLon();
            List<Location> locations = locationRepository.findByLatAndLon(lat, lon);

            if (locations.isEmpty())
                event.setLocation(locationRepository.save(new Location(null, lat, lon)));
            else
                event.setLocation(locations.get(0));
        }

        Boolean paid = updateEventCommonRequest.getPaid();
        if (paid != null)
            event.setPaid(paid);

        Integer participantLimit = updateEventCommonRequest.getParticipantLimit();
        if (participantLimit != null)
            event.setParticipantLimit(participantLimit);

        Boolean requestModeration = updateEventCommonRequest.getRequestModeration();
        if (requestModeration != null)
            event.setRequestModeration(requestModeration);

        String title = updateEventCommonRequest.getTitle();
        if (title != null)
            event.setTitle(title);

        return event;
    }


    public List<EventShortDto> eventToEventShortDtoList(List<Event> events) {

        Map<Long, Long> views = getViews(events);

        List<EventShortDto> eventShortDtos = new ArrayList<>();
        events.forEach(event -> eventShortDtos.add(eventToEventShortDto(event, views.get(event.getId()))));
        return eventShortDtos;
    }


    public List<EventFullDto> eventToEventFullDtoList(List<Event> events) {
        List<EventFullDto> eventFullDtos = new ArrayList<>();
        if (events.isEmpty())
            return eventFullDtos;

        Map<Long, Long> views = getViews(events);
        events.forEach(event -> eventFullDtos.add(eventToEventFullDto(event, views.get(event.getId()))));
        return eventFullDtos;
    }


    @SneakyThrows
    private Map<Long, Long> getViews(List<Event> events) {

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now();

        List<String> uris = new ArrayList<>();
        events.forEach(event -> uris.add("/events/" + event.getId()));

        ResponseEntity<Object> objResults = statsClient.getPeriodUrisStats(start, end, uris);
        List<EndpointStatisticsDto> endpointStatisticsDtos = objectMapper.readValue(
                objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});

        Map<Long, Long> views = new HashMap<>();
        endpointStatisticsDtos.forEach(endpointStatisticsDto -> {
            String uri = endpointStatisticsDto.getUri();
            if (!uri.equals("[]")) {
                if (uri.startsWith("["))
                    uri = uri.substring(1);
                if (uri.endsWith("]"))
                    uri = uri.substring(0, uri.length() - 1);
                Long eventId = Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
                views.put(eventId, endpointStatisticsDto.getHits());
            }
        });
        return views;
    }


}
