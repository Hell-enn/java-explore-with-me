package ru.practicum.explorewithme;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Модель данных статистической информации о запросах к определенным эндпоинтам
 * главного микросервиса приложения, отправляемая в теле HTTP-ответа.
 * Содержит поля:
 *  app - строка, содержащая наименование микросервиса, в котором содержатся эндпоинты,
 *          по которым собирается статистика,
 *  uri - префикс эндпоинта, по которому собирается статистика,
 *  hits - количество отправленных к экдпоинту с префиксом uri запросов.
 */
@Data
@AllArgsConstructor
public class EndpointStatisticsDto {
    @NotNull
    private String app;
    @NotNull
    private String uri;
    @NotNull
    private Long hits;
}
