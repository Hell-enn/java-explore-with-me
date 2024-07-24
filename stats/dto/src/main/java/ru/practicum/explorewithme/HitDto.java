package ru.practicum.explorewithme;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

/**
 * Модель данных информации о конкретном запросе к эндпоинтам
 * главного микросервиса приложения, приходящая в теле HTTP-запроса к клиенту.
 * Содержит поля:
 *  id - идентификатор запроса к эндпоинту
 *  app - строка, содержащая наименование микросервиса, в котором содержатся эндпоинты
 *  uri - префикс эндпоинта
 *  ip - строка, содержащая информацию о местоположении пользователя
 *  timestamp - момент времени, в который был сделан запрос к эндпоинту.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HitDto {
    private Long id;
    @NotNull
    private String app;
    @NotNull
    private String uri;
    @NotNull
    private String ip;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent
    private LocalDateTime timestamp;
}