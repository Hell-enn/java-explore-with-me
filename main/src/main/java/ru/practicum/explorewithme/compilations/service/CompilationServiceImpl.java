package ru.practicum.explorewithme.compilations.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.RequestAmountDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilations.model.Compilation;
import ru.practicum.explorewithme.compilations.model.EventCompilation;
import ru.practicum.explorewithme.compilations.repository.CompilationRepository;
import ru.practicum.explorewithme.compilations.repository.EventCompilationRepository;
import ru.practicum.explorewithme.events.dto.EndpointStatisticsDto;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exceptions.BadRequestException;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.requests.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventCompilationRepository eventCompilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        List<Boolean> pinnedList = pinned == null ? List.of(true, false) : List.of(pinned);
        int amountOfCompilations = compilationRepository.findPinnedAmount(pinnedList);
        int pageNum = amountOfCompilations > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<Compilation> compilations = compilationRepository.findByPinnedOrderById(pinnedList, page);

        Map<Long, List<EndpointStatisticsDto>> endpointStatisticsDtoMap = new HashMap<>();
        Map<Long, List<Event>> eventsMap = new HashMap<>();
        Map<Long, List<RequestAmountDto>> requestAmountDtoMap = new HashMap<>();

        List<Long> eventIds = new ArrayList<>();
        List<Event> events = new ArrayList<>();
        List<Long> compIds = new ArrayList<>();
        Map<Long, Long> eventCompilationPairs = new HashMap<>();

        compilations.forEach(compilation -> compIds.add(compilation.getId()));
        List<EventCompilation> eventCompilations = eventCompilationRepository.findEventCompilationsByCompIds(compIds);
        eventCompilations.forEach(eventCompilation -> {
            Long eventId = eventCompilation.getEvent().getId();
            Long compId = eventCompilation.getCompilation().getId();
            eventCompilationPairs.put(eventId, compId);
        });

        Map<Long, RequestAmountDto> requestAmountMap = new HashMap<>();
        List<RequestAmountDto> requestAmountDtos = participationRequestRepository.findRequestAmount(eventIds);
        requestAmountDtos.forEach(requestAmountDto -> {
            if (requestAmountDto != null)
                requestAmountMap.put(requestAmountDto.getEventId(), requestAmountDto);
        });

        eventCompilations.forEach(eventCompilation -> {
            Long key = eventCompilation.getCompilation().getId();
            List<Event> value = eventsMap.getOrDefault(key, new ArrayList<>());
            Event eventToAdd = eventCompilation.getEvent();
            if (!value.contains(eventToAdd))
                value.add(eventToAdd);
            eventsMap.put(key, value);

            List<RequestAmountDto> requestAmountDtoEvents = new ArrayList<>();

            value.forEach(val -> {
                Long eventId = val.getId();
                eventIds.add(eventId);
                requestAmountDtoEvents.add(requestAmountMap.get(eventId));
                if (!events.contains(val))
                    events.add(val);
            });

            requestAmountDtoMap.put(key, requestAmountDtoEvents);
        });

        List<EndpointStatisticsDto> endpointStatisticsDtos = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                events);

        endpointStatisticsDtos.forEach(endpointStatisticsDto -> {
            String uri = endpointStatisticsDto.getUri();
            if (uri != null && !uri.equals("[]")) {
                Long eventId;
                if (uri.startsWith("["))
                    uri = uri.substring(1);
                if (uri.endsWith("]"))
                    uri = uri.substring(0, uri.length() - 1);
                if (!(uri.endsWith("/") || uri.endsWith("s"))) {
                    eventId = Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                } else {
                    eventId = 0L;
                }
                Long compId = eventCompilationPairs.get(eventId);
                List<EndpointStatisticsDto> endpointStatisticsDtoList = endpointStatisticsDtoMap.getOrDefault(compId, new ArrayList<>());
                endpointStatisticsDtoList.add(endpointStatisticsDto);
                endpointStatisticsDtoMap.put(compId, endpointStatisticsDtoList);
            }
        });

        List<CompilationDto> compilationDtos = compilationMapper
                .compilationToCompilationDtoList(
                        compilations,
                        endpointStatisticsDtoMap,
                        eventsMap,
                        requestAmountDtoMap);
        log.debug("Возвращение  подборок с позиции {} в количестве {}:\n{}", from, size, compilationDtos);

        return compilationDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id = " + compId + " не найдена!"));

        List<Long> eventIds = eventRepository.findCompilationEventIds(compId);
        Iterable<Event> events = eventRepository.findAllById(eventIds);
        List<EndpointStatisticsDto> endpointStatisticsDtos = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                (List<Event>) events
        );

        List<Event> compilationEvents = eventRepository.findEventsByCompilation(compId);

        List<Long> compilationEventIds = new ArrayList<>();
        compilationEvents.forEach(compilationEvent -> compilationEventIds.add(compilationEvent.getId()));
        List<RequestAmountDto> requestAmountDtosList = participationRequestRepository.findRequestAmount(
                compilationEventIds);

        CompilationDto compilationDto = compilationMapper
                .compilationToCompilationDto(compilation, endpointStatisticsDtos, compilationEvents, requestAmountDtosList);
        log.debug("Возвращение подборки событий с id = {}", compId);

        return compilationDto;
    }


    @Override
    @Transactional
    public CompilationDto postCompilation(NewCompilationDto newCompilationDto) {

        if (newCompilationDto.getPinned() == null)
            newCompilationDto.setPinned(false);
        if (newCompilationDto.getTitle().trim().isEmpty())
            throw new BadRequestException("Строка с заголовком пустая!");

        List<Long> eventIds = newCompilationDto.getEvents();
        Collection<Event> eventsByIds = null;
        if (eventIds != null) {
            eventsByIds = (Collection<Event>) eventRepository.findAllById(eventIds);
            if (eventIds.size() > eventsByIds.size()) {
                Map<Long, Event> events = new HashMap<>();
                eventsByIds.forEach(event -> events.put(event.getId(), event));
                eventIds.forEach(eventId -> {
                    if (events.get(eventId) == null)
                        throw new NotFoundException("Событие с id = " + eventId + " не существует!");
                });
            }
        }

        Compilation savedCompilation =
                compilationRepository.save(compilationMapper.newCompilationDtoToCompilation(newCompilationDto));
        List<EventCompilation> eventCompilations = new ArrayList<>();
        if (eventsByIds != null) {
            eventsByIds.forEach(event -> eventCompilations.add(new EventCompilation(null, savedCompilation, event)));
            eventCompilationRepository.saveAll(eventCompilations);
        }

        List<EndpointStatisticsDto> endpointStatisticsDtos;
        if (eventsByIds != null) {
            endpointStatisticsDtos = getUniqueStats(
                            LocalDateTime.now().minusYears(100),
                            LocalDateTime.now().plusYears(100),
                            (List<Event>) eventsByIds);
        } else {
            endpointStatisticsDtos = new ArrayList<>();
        }

        List<RequestAmountDto> requestAmountDtos = participationRequestRepository.findRequestAmount(eventIds);

        CompilationDto compilationDto = compilationMapper
                .compilationToCompilationDto(
                        savedCompilation,
                        endpointStatisticsDtos,
                        (List<Event>) eventsByIds,
                        requestAmountDtos);
        log.debug("Публикация подборки событий прошла успешно!\n{}", compilationDto);

        return compilationDto;
    }


    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не существует!"));

        List<Long> compEventIds = eventCompilationRepository.findEventIdsByCompId(compId);
        if (!compEventIds.isEmpty())
            throw new ConflictException("К удаляемой подборке привязаны события!");

        compilationRepository.deleteById(compId);
        log.debug("Публикация подборки событий с id = {} прошло успешно!", compId);
    }


    @Override
    @Transactional
    public CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не существует!"));

        List<Long> eventIds = updateCompilationRequest.getEvents();
        if (eventIds != null) {
            Collection<Event> eventsByIds = (Collection<Event>) eventRepository.findAllById(eventIds);
            if (eventIds.size() > eventsByIds.size()) {
                Map<Long, Event> events = new HashMap<>();
                eventsByIds.forEach(event -> events.put(event.getId(), event));
                eventIds.forEach(eventId -> {
                    if (events.get(eventId) == null)
                        throw new NotFoundException("Событие с id = " + eventId + " не существует!");
                });
            }
            eventCompilationRepository.deleteByCompilationId(compId);
            List<EventCompilation> eventCompilations = new ArrayList<>();
            eventsByIds.forEach(event -> eventCompilations.add(new EventCompilation(null, compilation, event)));
            eventCompilationRepository.saveAll(eventCompilations);
        }

        String title = updateCompilationRequest.getTitle();
        if (title != null)
            compilation.setTitle(title);

        Boolean pinned = updateCompilationRequest.getPinned();
        if (pinned != null)
            compilation.setPinned(pinned);

        compilationRepository.save(compilation);

        List<Long> existedEventIds = eventCompilationRepository.findEventIdsByCompId(compId);
        Iterable<Event> events = existedEventIds != null && !existedEventIds.isEmpty() ?
                eventRepository.findAllById(existedEventIds) : new ArrayList<>();

        List<EndpointStatisticsDto> endpointStatisticsDtos = getUniqueStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                (List<Event>) events
        );

        List<RequestAmountDto> requestAmountDtos =
                participationRequestRepository.findRequestAmount(existedEventIds);


        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(
                compilation,
                endpointStatisticsDtos,
                (List<Event>) events,
                requestAmountDtos);
        log.debug("Обновление подборки событий прошло успешно!\n{}", compilationDto);

        return compilationDto;
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