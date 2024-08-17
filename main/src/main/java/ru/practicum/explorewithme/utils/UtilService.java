package ru.practicum.explorewithme.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.HitDto;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.compilations.dto.RequestAmountDto;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UtilService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public Map<Long, Long> findRequestAmountList(List<Long> eventIds) {
        Map<Long, Long> requestAmounts = new HashMap<>();
        List<RequestAmountDto> requestAmountDtos = participationRequestRepository.findRequestAmount(eventIds);
        requestAmountDtos.forEach(requestAmountDto -> requestAmounts.put(
                requestAmountDto.getEventId(),
                requestAmountDto.getRequestAmount()));
        return requestAmounts;
    }


    @SneakyThrows
    public List<EndpointStatisticsDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<Event> events) {
        List<String> uris = new ArrayList<>();
        events.forEach(event -> uris.add("/events/" + event.getId()));

        ResponseEntity<Object> objResults = statsClient
                .getPeriodUrisUniqueStats(start, end, uris, true);
        return objectMapper.readValue(objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});
    }


    @SneakyThrows
    public HitDto postHit(HttpServletRequest request) {
        ResponseEntity<Object> objResults = statsClient
                .postHit(request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        return objectMapper.readValue(objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});
    }
}
