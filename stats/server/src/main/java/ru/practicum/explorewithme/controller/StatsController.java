package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.HitDto;
import ru.practicum.explorewithme.service.HitService;

import java.util.List;

/**
 * Класс-контроллер StatsController серверной части микросервиса
 * принимает HTTP-запросы от клиентской части, касающиеся взаимодействия
 * с информацией о запросах из основного микросервиса и получения о ней статистики,
 * преобразует их в валидируемые объекты Java и маршрутизирует в слой
 * HitService, где содержится основная бизнес-логика по взаимодействию с описанными объектами.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {

    private final HitService hitService;


    /**
     * Эндпоинт. Метод получает запрос пользователя на сохранение информации о том,
     * что на uri главного сервиса был отправлен запрос на получение информации о событии(-ях),
     * парсит его в понятные java, валидируемые объекты:
     * @param hitDto (объект, содержащий информацию о запросе пользователя).
     * В рамках эндпоинта происходит маршрутизация на уровень сервиса.
     *
     * @return ResponseEntity<Object> (возвращаемый пользователю объект с информацией о запросе
     * или код ответа, отличный от 2**, с описанием причины возникновения ошибки)
     */
    @PostMapping("/hit")
    public ResponseEntity<HitDto> postHit(@RequestBody HitDto hitDto) {
        log.debug("Принят запрос на добавление информации о запросе о мероприятии\n{}", hitDto);
        HitDto hitDto1 = hitService.postHit(hitDto);
        ResponseEntity<HitDto> test = ResponseEntity.accepted().body(hitDto1);
        return test;
    }


    /**
     * Эндпоинт. Метод получает запрос пользователя на получение статистики запросов к эндпоинтам
     * с префиксами uris основного микросервиса, парсит его в понятные java, валидируемые объекты:
     * @param start (момент времени, с которого следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param end (момент времени, по который следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param uris (список префиксов эндпоинтов, по которым необходимо собрать статистически данные)
     * @param unique (флаг уникальности запроса от конкретного пользователя:
     *               true - рассматриваем только уникальные запросы, false - все запросы)
     *
     * В рамках эндпоинта происходит маршрутизация на уровень сервиса, взаимодействующего с JPA.
     *
     * @return ResponseEntity<Object> (возвращаемый пользователю список объектов статистики по эндпоинтам
     * с префиксами uris с момента start до момента end, или код ответа, отличный от 2**,
     * с описанием причины возникновения ошибки)
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.debug("Принят запрос на получение статистики запросов на получение информации о мероприятиях\n" +
                "start: {}\nend: {}\nuris: {}\nunique: {}", start, end, uris, unique);

        return ResponseEntity.ok().body(hitService.getStatistics(start, end, uris, unique));
    }
}