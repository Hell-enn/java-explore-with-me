package ru.practicum.explorewithme.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAmountDto {
    private Long eventId;
    private Long requestAmount;
}
