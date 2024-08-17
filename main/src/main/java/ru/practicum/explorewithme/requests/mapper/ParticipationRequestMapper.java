package ru.practicum.explorewithme.requests.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.requests.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.requests.model.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto participationRequestToParticipationRequestDto(ParticipationRequest participationRequest) {
        return new ParticipationRequestDto(
                participationRequest.getCreated(),
                participationRequest.getEvent().getId(),
                participationRequest.getId(),
                participationRequest.getUser().getId(),
                participationRequest.getStatus().toString()
        );
    }


    public List<ParticipationRequestDto> participationRequestToParticipationRequestDtoList(List<ParticipationRequest> participationRequests) {
        List<ParticipationRequestDto> participationRequestDtos = new ArrayList<>();
        participationRequests.forEach(participationRequest ->
                participationRequestDtos.add(participationRequestToParticipationRequestDto(participationRequest)));
        return participationRequestDtos;
    }
}
