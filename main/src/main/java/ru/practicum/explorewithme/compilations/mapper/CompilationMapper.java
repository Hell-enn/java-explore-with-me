package ru.practicum.explorewithme.compilations.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.model.Compilation;
import ru.practicum.explorewithme.requests.dto.enums.Status;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.dto.EventShortDto;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.users.dto.UserShortDto;
import ru.practicum.explorewithme.users.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class CompilationMapper {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public Compilation compilationDtoToCompilation(CompilationDto compilationDto) {
        return new Compilation(
                compilationDto.getId(),
                compilationDto.getPinned(),
                compilationDto.getTitle());
    }

    @SneakyThrows
    public CompilationDto compilationToCompilationDto(Compilation compilation) {

        Long compId = compilation.getId();
        List<EventShortDto> eventShortDtos = new ArrayList<>();

        List<Long> eventIds = eventRepository.findCompilationEventIds(compId);
        List<String> uris = new ArrayList<>();
        eventIds
                .forEach(eventId -> uris.add("/events/" + eventId));

        ResponseEntity<Object> objResults = statsClient.getPeriodUrisStats(
                LocalDateTime.now().minusYears(5L),
                LocalDateTime.now(),
                uris);
        List<EndpointStatisticsDto> endpointStatisticsDtos = objectMapper.readValue(
                objectMapper.writeValueAsString(objResults.getBody()), new TypeReference<>() {});

        Map<Long, Long> idsWithHits = new HashMap<>();
        if (endpointStatisticsDtos != null)
            endpointStatisticsDtos
                    .forEach(endpointStatisticsDto -> {
                        String uri = endpointStatisticsDto.getUri();
                        if (!uri.equals("[]")) {
                            if (uri.startsWith("["))
                                uri = uri.substring(1);
                            if (uri.endsWith("]"))
                                uri = uri.substring(0, uri.length() - 1);
                            Long eventId = Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                            idsWithHits.put(eventId, endpointStatisticsDto.getHits());
                        }
                    });

        eventRepository.findEventsByCompilation(compId)
                .forEach(event -> {
                    Long eventId = event.getId();

                    Category category = event.getCategory();
                    CategoryDto categoryDto = new CategoryDto(
                            category.getId(),
                            category.getName()
                    );

                    int confirmedRequests = participationRequestRepository.findRequestAmount(eventId, Status.CONFIRMED.toString());

                    User user = event.getUser();
                    UserShortDto userShortDto = new UserShortDto(
                            user.getId(),
                            user.getName()
                    );

                    EventShortDto eventShortDto = new EventShortDto(
                            eventId,
                            event.getAnnotation(),
                            categoryDto,
                            confirmedRequests,
                            event.getEventDate(),
                            userShortDto,
                            event.getPaid(),
                            event.getTitle(),
                            idsWithHits.get(eventId)
                    );
                    eventShortDtos.add(eventShortDto);
                });


        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventShortDtos);
    }

    public List<CompilationDto> compilationToCompilationDtoList(List<Compilation> compilations) {
        List<CompilationDto> compilationDtos = new ArrayList<>();
        compilations.forEach(compilation -> compilationDtos.add(compilationToCompilationDto(compilation)));
        return compilationDtos;
    }


    public Compilation newCompilationDtoToCompilation(NewCompilationDto newCompilationDto) {
        return new Compilation(
                null,
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle());
    }
}
