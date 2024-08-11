package ru.practicum.explorewithme.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.events.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Future;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicEventController {

    private final EventService eventServiceImpl;

    @GetMapping
    public ResponseEntity<Object> getPublicEvents(@RequestParam(required = false) String text,
                                                 @RequestParam(required = false) List<Long> categories,
                                                 @RequestParam(required = false) Boolean paid,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Future LocalDateTime rangeEnd,
                                                 @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                 @RequestParam(defaultValue = "") String sort,
                                                 @RequestParam(defaultValue = "0")Integer from,
                                                 @RequestParam(defaultValue = "10")Integer size,
                                                 HttpServletRequest request) {
        log.debug("Принят запрос на получение списка событий по параметрам:" +
                "\n\tподстрока: {}" +
                "\n\tкатегории: {}" +
                "\n\tфлаг оплаты: {}" +
                "\n\tдата начала поиска: {}" +
                "\n\tдата конца поиска: {}" +
                "\n\tфлаг наличия свободных мест: {}" +
                "\n\tсортировка по: {}" +
                "\nс позиции {} в количестве {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getPublicEvents(
                        text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getPublicEvent(@PathVariable Long id, HttpServletRequest request) {
        log.debug("Принят запрос на получение события с id = {}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getPublicEvent(id, request));
    }
}
