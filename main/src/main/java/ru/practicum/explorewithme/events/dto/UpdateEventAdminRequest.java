package ru.practicum.explorewithme.events.dto;

import lombok.*;
import ru.practicum.explorewithme.events.dto.annotations.ValidatedStateActionEnum;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventCommonRequest {
    @ValidatedStateActionEnum(acceptedValues = {"PUBLISH_EVENT", "REJECT_EVENT"})
    private String stateAction;
}