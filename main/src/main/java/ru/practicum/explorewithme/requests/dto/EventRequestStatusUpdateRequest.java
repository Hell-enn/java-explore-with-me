package ru.practicum.explorewithme.requests.dto;

import lombok.*;
import ru.practicum.explorewithme.requests.dto.annotations.ValidatedStatusEnum;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    @ValidatedStatusEnum(acceptedValues = {"REJECTED", "CONFIRMED"})
    private String status;
}