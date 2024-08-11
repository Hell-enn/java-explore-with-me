package ru.practicum.explorewithme.events.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    private Double lat;
    private Double lon;
}