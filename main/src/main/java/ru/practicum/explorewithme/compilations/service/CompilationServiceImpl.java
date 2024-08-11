package ru.practicum.explorewithme.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilations.model.Compilation;
import ru.practicum.explorewithme.compilations.model.EventCompilation;
import ru.practicum.explorewithme.compilations.repository.CompilationRepository;
import ru.practicum.explorewithme.compilations.repository.EventCompilationRepository;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exceptions.BadRequestException;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventCompilationRepository eventCompilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        List<Boolean> pinnedList = pinned == null ? List.of(true, false) : List.of(pinned);
        int amountOfCompilations = compilationRepository.findPinnedAmount(pinnedList);
        int pageNum = amountOfCompilations > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<CompilationDto> compilationDtos = compilationMapper
                .compilationToCompilationDtoList(compilationRepository.findByPinnedOrderById(pinnedList, page));
        log.debug("Возвращение  подборок с позиции {} в количестве {}:\n{}", from, size, compilationDtos);

        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id = " + compId + " не найдена!"));

        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(compilation);
        log.debug("Возвращение подборки событий с id = {}", compId);

        return compilationDto;
    }


    @Override
    public CompilationDto postCompilation(NewCompilationDto newCompilationDto) {

        if (newCompilationDto.getPinned() == null)
            newCompilationDto.setPinned(false);
        if (newCompilationDto.getTitle().trim().isEmpty())
            throw new BadRequestException("Строка с заголовком пустая!");

        List<Long> eventIds = newCompilationDto.getEvents();
        Iterable<Event> events = new ArrayList<>();
        if (eventIds != null) {
            eventIds.forEach(event -> {
                if (!eventRepository.existsById(event))
                    throw new NotFoundException("Событие с id = " + event + " не существует!");
            });
            events = eventRepository.findAllById(eventIds);
        }

        Compilation savedCompilation =
                compilationRepository.save(compilationMapper.newCompilationDtoToCompilation(newCompilationDto));
        for (Event event: events)
            eventCompilationRepository.save(new EventCompilation(null, savedCompilation, event));

        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(savedCompilation);
        log.debug("Публикация подборки событий прошла успешно!\n{}", compilationDto);

        return compilationDto;
    }


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
    public CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не существует!"));

        List<Long> eventIds = updateCompilationRequest.getEvents();
        List<Long> uniqueEventIds = new ArrayList<>();
        if (eventIds != null) {
            eventIds.forEach(eventId -> {
                if (!uniqueEventIds.contains(eventId)) {
                    if (!eventRepository.existsById(eventId))
                        throw new NotFoundException("Событие с id = " + eventId + " не существует!");
                    uniqueEventIds.add(eventId);
                }
            });
            eventCompilationRepository.deleteByCompilationId(compId);
            uniqueEventIds.forEach(uniqueEventId ->
                    eventCompilationRepository.save(new EventCompilation(null, compilation, eventRepository.findById(uniqueEventId).get()))
            );
        }

        String title = updateCompilationRequest.getTitle();
        if (title != null)
            compilation.setTitle(title);

        Boolean pinned = updateCompilationRequest.getPinned();
        if (pinned != null)
            compilation.setPinned(pinned);

        compilationRepository.save(compilation);

        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(compilation);
        log.debug("Обновление подборки событий прошло успешно!\n{}", compilationDto);

        return compilationDto;
    }
}