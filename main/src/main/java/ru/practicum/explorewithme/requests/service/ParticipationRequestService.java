package ru.practicum.explorewithme.requests.service;

import ru.practicum.explorewithme.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto postUserRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserParticipationRequest(Long userId, Long requestId);
}
