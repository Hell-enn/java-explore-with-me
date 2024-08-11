package ru.practicum.explorewithme.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.requests.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.events.dto.enums.State;
import ru.practicum.explorewithme.requests.dto.enums.Status;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.requests.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.requests.model.ParticipationRequest;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        List<ParticipationRequest> participationRequests = participationRequestRepository.findByUserId(userId);

        List<ParticipationRequestDto> participationRequestDtos = participationRequestMapper
                .participationRequestToParticipationRequestDtoList(participationRequests);
        log.debug("Возвращение списка запросов пользователя с id = {} на участие в событии прошло успешно:\n{}",
                userId, participationRequestDtos);

        return participationRequestDtos;
    }


    @Override
    public ParticipationRequestDto postUserRequest(Long userId, Long eventId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id = " + eventId + " не найдено!"));

        List<ParticipationRequest> participationRequests = participationRequestRepository.findByEventIdAndUserId(eventId, userId);
        if (!participationRequests.isEmpty())
            throw new ConflictException("Заявка пользователя с id = " + userId + " на участие в мероприятии c id = " + eventId + " уже существует!");

        if (userId.equals(event.getUser().getId()))
            throw new ConflictException("Нельзя отправлять заявку на участие в собственном мероприятии!");

        if (!event.getState().equals(State.PUBLISHED))
            throw new ConflictException("Нельзя участвовать в неопубликованном событии!");

        if (event.getParticipantLimit() > 0 &&
                event.getParticipantLimit() == participationRequestRepository
                        .findRequestAmount(eventId, Status.CONFIRMED.toString()))
            throw new ConflictException("У события достигнут лимит запросов на участие!");

        Status status = event.getRequestModeration() && event.getParticipantLimit() != 0 ?
                Status.PENDING : Status.CONFIRMED;
        ParticipationRequest participationRequest = new ParticipationRequest(
                null, LocalDateTime.now(), event, user, status);

        ParticipationRequestDto participationRequestDto = participationRequestMapper
                .participationRequestToParticipationRequestDto(participationRequestRepository.save(participationRequest));
        log.debug("Публикация запроса пользователя с id = {} на участие в событии с id = {} прошла успешно!\n{}",
                userId, eventId, participationRequestDto);

        return participationRequestDto;
    }


    @Override
    public ParticipationRequestDto cancelUserParticipationRequest(Long userId, Long requestId) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));

        ParticipationRequest participationRequest = participationRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие с id = " + requestId + " не найден!"));

        participationRequest.setStatus(Status.CANCELED);
        ParticipationRequestDto participationRequestDto =
                participationRequestMapper.participationRequestToParticipationRequestDto(participationRequest);

        log.debug("Отклонение запроса пользователя с id = {} самим пользователем на участие в событии с id = {} прошло успешно!",
                userId, requestId);

        return participationRequestDto;
    }
}