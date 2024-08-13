package ru.practicum.explorewithme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * Класс StatsClient - клиентский слой микросервиса-статистики приложения,
 * отвечающий за преобразование маршрутизированных из слоя-контроллера
 * объектов, содержащих информацию о запросах, и прочих необходимых объектов и идентификаторов
 * в HTTP-запросы к главному микросервису, где содержится основная бизнес-логика
 * приложения. Здесь же инкапсулирована логика формирования
 * HTTP-запросов к главному микросервису приложения.
 * Поля:
 *      rest - объект класса RestTemplate, получаемый с помощью билдера в конструкторе.
 *  Предназначен для формирования и последующей отправки Http-запроса на серверную часть.
 *      formatter - объект класса DateTimeFormatter, используемый для преобразования объектов
 *  типа LocalDateTime к строке.
 */
@Service
public class StatsClient {
    protected final RestTemplate rest;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(RestTemplateBuilder builder) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory("http://stats-server:9090"))
                .requestFactory(ClientHttpRequestFactory.class)
                .build();
    }


    /**
     * Метод формирует запрос на публикацию нового объекта с информацией о запросах
     * к серверной части микросервиса с помощью закрытых методов с логикой непосредственного
     * формирования запроса.
     * @param uri (строка, содержащая префикс эндпоинта, в отношении которого должен
     *            быть сохранен объект с информацией)
     * @param ip (строка, содержащая ip пользователя, который сделал данный запрос)
     * @param moment (объект типа LocalDateTime, содержащий информацию о моменте времени,
     *               в который был произведен запрос)
     *
     * @return ResponseEntity<Object> - ответ сервера, содержащий либо код ответа 2** и
     * опубликованный объект с информацией о запросе с инициализированным полем-идентификатором,
     * либо иной код ответа с сообщением об ошибке.
     */
    public ResponseEntity<Object> postHit(String uri, String ip, LocalDateTime moment) {
        HitDto hitDto = new HitDto(null, "ewm-main-service", uri, ip, moment);
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDto);
    }


    /**
     * Метод формирует запрос на получение списка объектов со статистической информацией
     * о неуникальных запросах ко всем эндпоинтам в период с момента start
     * по момент end к серверной части микросервиса приложения.
     * @param start (момент времени, с которого следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param end (момент времени, по который следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     *
     * @return ResponseEntity<Object> - ответ сервера, содержащий либо код ответа 2** и
     * объект со статистической информацией, либо иной код ответа с сообщением об ошибке.
     */
    public ResponseEntity<Object> getPeriodStats(LocalDateTime start, LocalDateTime end) {

        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter));
        return makeAndSendRequest(HttpMethod.GET, "/stats?start={start}&end={end}", parameters, null);
    }


    /**
     * Метод формирует запрос на получение списка объектов со статистической информацией
     * о неуникальных запросах к переданным в uris эндпоинтам в период с момента start
     * по момент end к серверной части микросервиса приложения.
     * @param start (момент времени, с которого следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param end (момент времени, по который следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param uris (список префиксов эндпоинтов, по которым необходимо собрать статистически данные)
     *
     * @return ResponseEntity<Object> - ответ сервера, содержащий либо код ответа 2** и
     * объект со статистической информацией, либо иной код ответа с сообщением об ошибке.
     */
    public ResponseEntity<Object> getPeriodUrisStats(LocalDateTime start, LocalDateTime end, List<String> uris) {

        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", uris
        );

        return makeAndSendRequest(
                HttpMethod.GET,
                "/stats?start={start}&end={end}&uris={uris}",
                parameters,
                null);
    }


    /**
     * Метод формирует запрос на получение списка объектов со статистической информацией
     * об уникальных/неуникальных запросах пользователей к переданным в uris эндпоинтам в
     * период с момента start по момент end к серверной части микросервиса приложения.
     * @param start (момент времени, с которого следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param end (момент времени, по который следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param uris (список префиксов эндпоинтов, по которым необходимо собрать статистически данные)
     * @param unique (флаг уникальности запроса от конкретного пользователя:
     *               true - рассматриваем только уникальные запросы, false - все запросы)
     *
     * @return ResponseEntity<Object> - ответ сервера, содержащий либо код ответа 2** и
     * объект со статистической информацией, либо иной код ответа с сообщением об ошибке.
     */
    public ResponseEntity<Object> getPeriodUrisUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", uris,
                "unique", unique
        );
        return makeAndSendRequest(
                HttpMethod.GET,
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                parameters,
                null);
    }


    /**
     * Метод формирует запрос на получение списка объектов со статистической информацией
     * об уникальных/неуникальных запросах пользователей ко всем эндпоинтам в
     * период с момента start по момент end к серверной части микросервиса приложения.
     * @param start (момент времени, с которого следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param end (момент времени, по который следует осуществлять поиск статистки
     *              по сохраненной информации о запросах)
     * @param unique (флаг уникальности запроса от конкретного пользователя:
     *               true - рассматриваем только уникальные запросы, false - все запросы)
     *
     * @return ResponseEntity<Object> - ответ сервера, содержащий либо код ответа 2** и
     * объект со статистической информацией, либо иной код ответа с сообщением об ошибке.
     */
    public ResponseEntity<Object> getPeriodUniqueStats(String start, String end, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique
        );
        return makeAndSendRequest(
                HttpMethod.GET,
                "/stats?start={start}&end={end}&unique={unique}",
                parameters,
                null);
    }


    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> serverResponse;
        try {
            if (parameters != null) {
                serverResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                serverResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(serverResponse);
    }


    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }


    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
