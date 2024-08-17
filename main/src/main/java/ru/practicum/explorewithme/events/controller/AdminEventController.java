package ru.practicum.explorewithme.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.events.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminEventController {

    private final EventService eventServiceImpl;

    @GetMapping
    public ResponseEntity<Object> getEvents(@RequestParam(required = false) List<Long> users,
                                            @RequestParam(required = false) List<String> states,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        log.debug("""
                Принят запрос на получение админом списка событий по параметрам:
                \tавторы: {}
                \tсостояния: {}
                \tкатегории: {}
                \tдата начала поиска: {}
                \tдата конца поиска: {}
                с позиции {} в количестве {}""", users, states, categories, rangeStart, rangeEnd, from, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<Object> patchEvent(@PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.debug("""
                        Принят запрос на обновление админом события с id = {} объектом:
                        \tаннотация: {}
                        \tкатегория: {}
                        \tописание: {}
                        \tдата события: {}
                        \tлокация: {}
                        \tфлаг оплаты: {}
                        \tмаксимальное число участников: {}
                        \tфлаг необходимости модерации: {}
                        \tзаголовок: {}
                        \tдействие: {}""",
                eventId,
                updateEventAdminRequest.getAnnotation(),
                updateEventAdminRequest.getCategory(),
                updateEventAdminRequest.getDescription(),
                updateEventAdminRequest.getEventDate(),
                updateEventAdminRequest.getLocation(),
                updateEventAdminRequest.getPaid(),
                updateEventAdminRequest.getParticipantLimit(),
                updateEventAdminRequest.getRequestModeration(),
                updateEventAdminRequest.getTitle(),
                updateEventAdminRequest.getStateAction());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServiceImpl.patchEventByAdmin(eventId, updateEventAdminRequest));
    }
}