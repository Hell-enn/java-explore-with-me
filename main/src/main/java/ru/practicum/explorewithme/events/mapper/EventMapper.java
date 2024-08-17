package ru.practicum.explorewithme.events.mapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.events.dto.enums.State;
import ru.practicum.explorewithme.events.dto.enums.StateAction;
import ru.practicum.explorewithme.events.dto.*;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.model.Location;
import ru.practicum.explorewithme.users.dto.UserShortDto;
import ru.practicum.explorewithme.users.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class EventMapper {

    public Event newEventDtoToEvent(NewEventDto newEventDto,
                                    Category category,
                                    Location location,
                                    User user) {

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


    private EventFullDto eventToEventFullDto(Event event, Long views, Long requestAmount) {

        Category category = event.getCategory();
        CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName());

        User user = event.getUser();
        UserShortDto userShortDto = new UserShortDto(user.getId(), user.getName());

        Location location = event.getLocation();
        LocationDto locationDto = new LocationDto(location.getLat(), location.getLon());

        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                categoryDto,
                requestAmount == null ? null : Math.toIntExact(requestAmount),
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
    public EventFullDto eventToEventFullDto(Event event,
                                            List<EndpointStatisticsDto> endpointStatisticsDtos,
                                            Long requestAmount) {

        EndpointStatisticsDto endpointStatisticsDto;
        if (endpointStatisticsDtos != null && !endpointStatisticsDtos.isEmpty())
            endpointStatisticsDto = endpointStatisticsDtos.get(0);
        else
            endpointStatisticsDto = new EndpointStatisticsDto("ewm-main-service", "/event/" + event.getId(), 0L);
        Long views = endpointStatisticsDto == null ? 0 : endpointStatisticsDto.getHits();
        return eventToEventFullDto(event, views, requestAmount);
    }


    public EventShortDto eventToEventShortDto(Event event, Long views, Long requestAmount) {

        Category category = event.getCategory();
        CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName());

        User user = event.getUser();
        UserShortDto userShortDto = new UserShortDto(user.getId(), user.getName());

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryDto,
                requestAmount == null ? null : Math.toIntExact(requestAmount),
                event.getEventDate(),
                userShortDto,
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public Event updateEventAdmin(UpdateEventAdminRequest updateEventAdminRequest,
                                  Event event,
                                  Category category,
                                  Location location) {

        Event updatedEvent = updateEventCommon(updateEventAdminRequest, event, category, location);

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

    public Event updateEventUser(UpdateEventUserRequest updateEventUserRequest,
                                 Event event,
                                 Category category,
                                 Location location) {

        Event updatedEvent = updateEventCommon(updateEventUserRequest, event, category, location);

        String stateAction = updateEventUserRequest.getStateAction();
        if (stateAction != null) {
            if (stateAction.equals(StateAction.CANCEL_REVIEW.toString()))
                updatedEvent.setState(State.CANCELED);
            else if (stateAction.equals(StateAction.SEND_TO_REVIEW.toString()))
                updatedEvent.setState(State.PENDING);
        }

        return updatedEvent;
    }


    private Event updateEventCommon(UpdateEventCommonRequest updateEventCommonRequest,
                                    Event event,
                                    Category category,
                                    Location location) {
        String annotation = updateEventCommonRequest.getAnnotation();
        if (annotation != null && annotation.length() >= 20 && annotation.length() <= 2000)
            event.setAnnotation(annotation);

        if (category != null)
            event.setCategory(category);

        String description = updateEventCommonRequest.getDescription();
        if (description != null && description.length() >= 20 && description.length() <= 7000)
            event.setDescription(description);

        LocalDateTime eventDate = updateEventCommonRequest.getEventDate();
        if (eventDate != null)
            event.setEventDate(eventDate);

        if (location != null)
            event.setLocation(location);

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


    public List<EventShortDto> eventToEventShortDtoList(List<Event> events,
                                                        Map<Long, Long> requestAmount,
                                                        List<EndpointStatisticsDto> endpointStatisticsDtos) {

        Map<Long, Long> views = getViews(endpointStatisticsDtos);

        List<EventShortDto> eventShortDtos = new ArrayList<>();
        events.forEach(event ->
                eventShortDtos.add(eventToEventShortDto(event, views.get(event.getId()), requestAmount.get(event.getId()))));
        return eventShortDtos;
    }


    public List<EventFullDto> eventToEventFullDtoList(List<Event> events,
                                                      List<EndpointStatisticsDto> endpointStatisticsDtos,
                                                      Map<Long, Long> requestAmounts) {
        List<EventFullDto> eventFullDtos = new ArrayList<>();
        if (events.isEmpty())
            return eventFullDtos;

        events.forEach(event -> eventFullDtos.add(
                eventToEventFullDto(
                        event,
                        endpointStatisticsDtos,
                        requestAmounts.getOrDefault(event.getId(), 0L))));
        return eventFullDtos;
    }


    @SneakyThrows
    private Map<Long, Long> getViews(List<EndpointStatisticsDto> endpointStatisticsDtos) {
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
