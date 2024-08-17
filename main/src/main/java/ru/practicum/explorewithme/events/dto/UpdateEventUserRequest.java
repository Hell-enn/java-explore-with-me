package ru.practicum.explorewithme.events.dto;

import lombok.*;
import ru.practicum.explorewithme.events.dto.annotations.ValidatedStateActionEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEventCommonRequest {
    @ValidatedStateActionEnum(acceptedValues = {"CANCEL_REVIEW"})
    private String stateAction;
}
