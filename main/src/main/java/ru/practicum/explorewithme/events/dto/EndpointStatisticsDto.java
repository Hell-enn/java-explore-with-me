package ru.practicum.explorewithme.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointStatisticsDto {
    private String app;
    private String uri;
    private Long hits;
}
