package ru.practicum.explorewithme.events.service;

import ru.practicum.explorewithme.events.dto.*;
import ru.practicum.explorewithme.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto postEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto patchEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult patchUserEventRequest(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<EventFullDto> getAdminEvents(List<Long> users,
                                 List<String> states,
                                 List<Long> categories,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Integer from,
                                 Integer size);

    EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> getPublicEvents(String text,
                                       List<Long> categories,
                                       Boolean paid,
                                       LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd,
                                       Boolean onlyAvailable,
                                       String sort,
                                       Integer from,
                                       Integer size,
                                       HttpServletRequest request);

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);
}
