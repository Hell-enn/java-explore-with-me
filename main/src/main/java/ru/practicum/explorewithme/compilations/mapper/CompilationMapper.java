package ru.practicum.explorewithme.compilations.mapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.RequestAmountDto;
import ru.practicum.explorewithme.compilations.model.Compilation;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.dto.EventShortDto;
import ru.practicum.explorewithme.users.dto.UserShortDto;
import ru.practicum.explorewithme.users.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class CompilationMapper {

    @SneakyThrows
    public CompilationDto compilationToCompilationDto(Compilation compilation,
                                                      List<EndpointStatisticsDto> endpointStatisticsDtos,
                                                      List<Event> compilationEvents,
                                                      List<RequestAmountDto> requestAmountDtosList) {

        List<EventShortDto> eventShortDtos = new ArrayList<>();

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
                            Long eventId = uri.endsWith("events") ? 0 : Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                            idsWithHits.put(eventId, endpointStatisticsDto.getHits());
                        }
                    });

        Map<Long, Long> confirmedRequests = new HashMap<>();
        requestAmountDtosList.forEach(requestAmountDto -> {
            if (requestAmountDto != null)
                confirmedRequests.put(requestAmountDto.getEventId(), requestAmountDto.getRequestAmount());
        });

        if (compilationEvents != null) {
            compilationEvents.forEach(event -> {
                Long eventId = event.getId();

                Category category = event.getCategory();
                CategoryDto categoryDto = new CategoryDto(
                        category.getId(),
                        category.getName()
                );

                Long confirmedRequestsAmount = confirmedRequests.get(eventId);

                User user = event.getUser();
                UserShortDto userShortDto = new UserShortDto(
                        user.getId(),
                        user.getName()
                );

                EventShortDto eventShortDto = new EventShortDto(
                        eventId,
                        event.getAnnotation(),
                        categoryDto,
                        confirmedRequestsAmount == null ? null : Math.toIntExact(confirmedRequestsAmount),
                        event.getEventDate(),
                        userShortDto,
                        event.getPaid(),
                        event.getTitle(),
                        idsWithHits.get(eventId)
                );
                eventShortDtos.add(eventShortDto);
            });
        }

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventShortDtos);
    }

    public List<CompilationDto> compilationToCompilationDtoList(List<Compilation> compilations,
                                                                Map<Long, List<EndpointStatisticsDto>> endpointStatisticsDtoMap,
                                                                Map<Long, List<Event>> eventsMap,
                                                                Map<Long, List<RequestAmountDto>> requestAmountDtoMap
                                                                ) {

        List<CompilationDto> compilationDtos = new ArrayList<>();
        compilations.forEach(compilation -> {
            Long compId = compilation.getId();
            compilationDtos.add(compilationToCompilationDto(
                    compilation,
                    endpointStatisticsDtoMap.getOrDefault(compId, new ArrayList<>()),
                    eventsMap.getOrDefault(compId, new ArrayList<>()),
                    requestAmountDtoMap.getOrDefault(compId, new ArrayList<>())));
        });
        return compilationDtos;
    }


    public Compilation newCompilationDtoToCompilation(NewCompilationDto newCompilationDto) {
        return new Compilation(
                null,
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle());
    }
}
